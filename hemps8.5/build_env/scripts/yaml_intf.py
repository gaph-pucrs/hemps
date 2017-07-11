#!/usr/bin/python
import yaml
import sys
from operator import attrgetter
from build_utils import ApplicationStartTime, get_app_task_name_list

## @package yaml_intf
#IMPORTANT: This file encapsulates the yaml reading process, abstracting this process to another modules
#For this reason, modification in the yaml sintax are only reflected here
#USE THIS FILE TO PROVIDE FUNCIONS TO ALL MODULES
#If you need to modify the yaml syntax, modify only this file, in a way to make
#the yaml reading process transparent to the other modules. This process is done by creating new functions in this module
#that can be called in the other modules

def get_yaml_reader(yaml_path):
    try:
        file = open(yaml_path, 'r')
    except:
        sys.exit('ERROR: No such following testcase file or directory (%s)!' % yaml_path)
    try:
        yaml_reader = yaml.load(file)
    except:
        sys.exit('ERROR: Incorrent YAML sintax!!\nThe YAML file does not support the character tab (\\t) (%s)!\n' % yaml_path)
    
    return yaml_reader

def get_page_size_KB(yaml_reader):
    return yaml_reader["hw"]["page_size_KB"]

def get_tasks_per_PE(yaml_reader):
    return yaml_reader["hw"]["tasks_per_PE"]

def get_memory_size_KB(yaml_reader):
    #page size number plus one (kernel page) x page_size_KB
    memory_size_KB = (get_tasks_per_PE(yaml_reader) + 1) * get_page_size_KB(yaml_reader)
    
    return memory_size_KB

def get_repository_size_MB(yaml_reader):
    return yaml_reader["hw"]["repository_size_MB"]

def get_repository_size_BYTES(yaml_reader):
    repo = get_repository_size_MB(yaml_reader) * 1024 * 1024
    return repo

def get_model_description(yaml_reader):
    return yaml_reader["hw"]["model_description"]

def get_noc_buffer_size(yaml_reader):
    return yaml_reader["hw"]["noc_buffer_size"]

def get_mpsoc_x_dim(yaml_reader):
    return yaml_reader["hw"]["mpsoc_dimension"][0]

def get_mpsoc_y_dim(yaml_reader):
    return yaml_reader["hw"]["mpsoc_dimension"][1]

def get_cluster_x_dim(yaml_reader):
    return yaml_reader["hw"]["cluster_dimension"][0]

def get_cluster_y_dim(yaml_reader):
    return yaml_reader["hw"]["cluster_dimension"][1]

def get_master_location(yaml_reader):
    return yaml_reader["hw"]["master_location"]

def get_mapping_algorithm(yaml_reader):
    return yaml_reader["sw"]["mapping_algorithm"]

def get_apps_name_list(yaml_reader):
   
    apps_list = yaml_reader["apps"]

    apps_name_list = []

    for app in apps_list:
        apps_name_list.append(app["name"])
        
    return apps_name_list

def get_apps_number(yaml_reader):
    number = len(get_apps_name_list(yaml_reader))
    return number

def get_task_scheduler(yaml_reader):
    return yaml_reader["sw"]["task_scheduler"]

def get_app_repo_size(yaml_reader):
    return 1000


#------- Repository Generation Scope ------------------- 
#ATTENTION: STATIC MAPPING ONLY WORKS IF THE APPS START TIME ARE ORDERED
#This function serches for all task statically mapped and return a list of tuple={task_id, proc}
def get_static_mapping_list(yaml_reader):
    
    static_task_list = []
    
    app_id = 0
    #walk for all apps into yaml file
    for app_n in yaml_reader["apps"]:
        
        app_name = app_n["name"]
        
        try:
            #Test if a given app have the static_mapping tag
            static_mapping_tasks = yaml_reader["apps"][app_id]["static_mapping"]
            
            #In this point can be concluded that the app have some static mapping
            #Then, list all tasks from app_name
            app_task_list = get_app_task_name_list(".", app_name)
            
            task_id = 0
            
            #Walk over all static tasks
            for static_task in static_mapping_tasks:
                
                if static_task in app_task_list:
                    
                    static_task_id = app_task_list.index(static_task)
                    
                    task_rel_id = app_id << 8 | static_task_id
                    x_address = int( static_mapping_tasks[static_task][0] ) # Gets the x value
                    y_address = int( static_mapping_tasks[static_task][1] ) # Gets the y value
                    
                    task_static_map = x_address << 8 | y_address
                    
                    static_task_list.append([task_rel_id, task_static_map])
                    
                else:
                    print "[WARNING]: Static task name ["+static_task+"] does not belong to application [" + app_name+ "], it will be ignored in static mapping\n"
        except:
            pass # This means that the application not has any task mapped statically
        
        app_id = app_id + 1
        
    return static_task_list
    

#Returns a list of objects ApplicationStartTime containing the app name, start time, and repo address
#However repo address return -1, it is filled inside app_builder during the repositoriy generation
def get_app_start_time_list(yaml_reader):
    
    app_start_time_list = []
    
    yaml_app_index = 0
    
    for app_reader in yaml_reader["apps"]:
        app_name = app_reader["name"]
        
        #If the time is not configured - default is zero
        try:
            start_time_ms = "%x" % int(app_reader["start_time_ms"])
        except:
            start_time_ms = "%x" % 0
        
        repo_address = "-1"
        
        app_start_time_list.append( ApplicationStartTime(app_name, start_time_ms.zfill(8), repo_address) )
    
    #Sort objects by start time. See more in : https://wiki.python.org/moin/HowTo/Sorting
    sorted_list = sorted(app_start_time_list, key=attrgetter('start_time_ms'))
    
    return sorted_list
        
