#!/usr/bin/env python
import sys
import os
import subprocess
import filecmp
from shutil import copyfile, rmtree
from os.path import join
from deloream_env import generate_deloream_env

## @package testcase_builder
# This module generates a self-contained testcase directory with all source files, makes, waves, and scripts inside.
# If the testcase dir already exists it only copies the diferent files.
# This script requeris only one argument, that is the path to the yaml file.
# The path of the generated testcase directory will be the current path that is calling the script. 
#IMPORTANT: This module also copies the python file inside the directory scripts. 
#    Those scripts must be called inside the testcase dir. Their alread haas been called by the makefile of the testcase

#http://stackoverflow.com/questions/4383571/importing-files-from-different-folder-in-python
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__))+"/scripts")
from yaml_intf import *
from build_utils import *

#When defined, a main function must be called in the last line of the script, take a look at the end of file
def main():
    #HEMPS_PATH - must to point to the root directory of hemps
    HEMPS_PATH = os.getenv("HEMPS_PATH", 0)
    
    #Test if testcase file HEMPS_PATH is valid
    if HEMPS_PATH == 0:
        sys.exit("ENV PATH ERROR: HEMPS_PATH not defined")
    
    #Test if testcase file is passed as arg1 by testing the lenght of argv list
    if len(sys.argv) <= 1 :
        sys.exit("ARG ERROR: arg1 must be a valid testcase file with a extension .yaml (<my_testcase>.yaml) ")
    
    #testcase name without .yaml
    INPUT_TESTCASE_FILE_PATH = sys.argv[1]

    if os.path.exists(INPUT_TESTCASE_FILE_PATH) == False:
    	sys.exit("ARG ERROR: arg1 must be a valid testcase file with a extension .yaml (<my_testcase>.yaml) ")
    
    #Gets the testcase name:
    path_list = INPUT_TESTCASE_FILE_PATH.split("/") #The testcase path can have severals '/' into its composition
    input_yaml_name = path_list[len(path_list)-1] #Gets the last element of the split list
    TESTCASE_NAME = input_yaml_name.split(".")[0]
    
    #Copies the simulation time in ms if exists
    simul_time = 1
    try:
        simul_time = int(sys.argv[2])
    except:
        pass    
    
    
    #Test if there are some differences between the input testcase file and the testcase file into testcase directory 
    #If there are differences, them deleted the testcase directory to rebuild a new testcase
    testcase_file_inside_dir = TESTCASE_NAME+"/"+TESTCASE_NAME+".yaml"
    if os.path.exists(testcase_file_inside_dir):
        testecase_changes = not filecmp.cmp(INPUT_TESTCASE_FILE_PATH, testcase_file_inside_dir)
        if testecase_changes:
            delete_if_exists(TESTCASE_NAME)
    
    
    #Clean all undesired simulation files traces
    delete_if_exists(TESTCASE_NAME+"/log")
    if os.path.exists(TESTCASE_NAME+"/log"):
        os.system("cd "+TESTCASE_NAME+"/log; rm -rf *")
    
    #Create the testcase path if not exist
    create_ifn_exists(TESTCASE_NAME)
    
    #Reads some importats parameters from testcase
    yaml_reader = get_yaml_reader(INPUT_TESTCASE_FILE_PATH)
    apps_name_list = get_apps_name_list(yaml_reader)
    model_description = get_model_description(yaml_reader)
    page_size_KB = get_page_size_KB(yaml_reader)
    memory_size_KB = get_memory_size_KB(yaml_reader)
    
    
    #Testcase generation: updates source files...
    copy_scripts ( HEMPS_PATH,  TESTCASE_NAME)
    copy_kernel( HEMPS_PATH,  TESTCASE_NAME)
    copy_apps( HEMPS_PATH,  TESTCASE_NAME,  apps_name_list)
    copy_hardware( HEMPS_PATH,  TESTCASE_NAME, model_description)
    copy_makefiles( HEMPS_PATH,  TESTCASE_NAME, page_size_KB, memory_size_KB, model_description, apps_name_list, simul_time)
    copy_testcase_file( TESTCASE_NAME, INPUT_TESTCASE_FILE_PATH)
    
    #Create other importatants dirs
    create_ifn_exists(TESTCASE_NAME+"/include")
    create_ifn_exists(TESTCASE_NAME+"/log")
    
    #Calls the deloream_env.py to generate all necessary debugging dir and files
    generate_deloream_env(TESTCASE_NAME, yaml_reader)
    
    #Calls the hemps-wave_gen script if
    generate_wave(INPUT_TESTCASE_FILE_PATH)
    
# ----------------------------------------- FUNCTIONS ---------------------------------------------

# This fucntion copies the source files in source_dir to target_dir
#If you desire to add especific copies test, for example, ignore some specific files names or extensions, 
#please includes those file name or extension into the 3rd argument (ignored_names_list), the name can be the file name or its extension
def generic_copy(source_dir, target_dir, ignored_extension_list):
    
    exclude_extensions = " --exclude=".join(ignored_extension_list)
    
    command_string = "rsync -u -r --exclude="+exclude_extensions+" "+source_dir+"/ "+target_dir+"/"
    
    status = os.system(command_string)

def copy_scripts(hemps_path, testcase_path):
    
    source_script_path = hemps_path+"/build_env/scripts"
    testcase_script_path = testcase_path+"/build"
    
    generic_copy(source_script_path, testcase_script_path, [".svn"] )

#This funtion copies the software source files to the testcase/software path. The copied files are kernel and apps
def copy_kernel(hemps_path, testcase_path):
    
    source_sw_path = hemps_path+"/software"
    testcase_sw_path = testcase_path+"/software"
    
    #--------------  COPIES ALL THE FILES .H AND .C FROM SOFTWARE DIRECTORY ----------------
    generic_copy(source_sw_path, testcase_sw_path, [".svn"] )
   

def copy_apps(hemps_path, testcase_path, apps_name_list):
    #--------------  COPIES ALL APP SOURCE FILES RELATED INTO TESTCASE FILE ----------------
    source_app_path = hemps_path+"/applications/"
    testcase_app_path = testcase_path+"/applications/"
    
    create_ifn_exists(testcase_app_path)
        
    #for each app described into testcase file
    for app_name in apps_name_list:

        source_app_dir = source_app_path + app_name
        target_app_dir = testcase_app_path + app_name
        
        generic_copy(source_app_dir, target_app_dir, [".svn"])
        
    apps_in_testcase = []
    
    #List as directories from applications directory
    for tc_app in os.listdir(testcase_app_path):
        if os.path.isdir(testcase_app_path+tc_app):
            apps_in_testcase.append(tc_app)
        
    #Remove the apps already present into testcase 
    to_remove_apps = list ( set(apps_in_testcase) - set(apps_name_list) )
    
    for to_remove_app in to_remove_apps:
        delete_if_exists(testcase_app_path + to_remove_app)
        
# This fucntion copies the source files in hardware dir according with the system model description
#For example, to a SytemC description, all files in the hardware dir with the extension .ccp and .h will be copied
#If you desire to add especific copies test, for example, ignore some specific vhd files, please include those file name or extension
#into the 3rd argument (ignored_names_list), the name can be the file name or its extension
def copy_hardware(hemps_path, testcase_path, system_model_description):
    
    source_hw_path = hemps_path+"/hardware"
    testcase_hw_path = testcase_path+"/hardware"
    
    #Creates the direcoty into testcase path
    create_ifn_exists(testcase_hw_path)
    
    if system_model_description == "sc" or system_model_description == "scmod":
        
        delete_if_exists(testcase_hw_path+"/vhdl")
        source_hw_path = source_hw_path+"/sc"
        testcase_hw_path = testcase_hw_path+"/sc"
        ignored_names_list = [".svn" , ".vhd"]
        
    elif system_model_description == "vhdl":
        
        delete_if_exists(testcase_hw_path+"/sc")
        source_hw_path = source_hw_path+"/vhdl"
        testcase_hw_path = testcase_hw_path+"/vhdl"
        ignored_names_list = [".svn" , ".h", ".cpp"]
        
    else:
        sys.exit('Error in system_model_description - you must provide a compatible system model description')
    
    generic_copy(source_hw_path, testcase_hw_path, ignored_names_list)

def copy_makefiles(hemps_path, testcase_path, page_size_KB, memory_size_KB, system_model_description, apps_list, simul_time):
     #--------------  COPIES THE MAKEFILE TO SOFTWARE DIR ----------------------------------
   
    makes_dir = hemps_path+"/build_env/makes"
    
    if system_model_description == "sc":
        
        copyfile(makes_dir+"/make_systemc", testcase_path+"/hardware/makefile")
        
        if os.path.isfile(testcase_path+"/sim.do"):
            os.remove(testcase_path+"/sim.do") 
        
    elif system_model_description == "vhdl":
        
         copyfile(makes_dir+"/make_vhdl", testcase_path+"/hardware/makefile")
         
         copyfile(makes_dir+"/sim.do", testcase_path+"/sim.do")
         
        
    elif system_model_description == "scmod":
        
        copyfile(makes_dir+"/make_systemc_mod", testcase_path+"/hardware/makefile")

        copyfile(makes_dir+"/sim.do", testcase_path+"/sim.do")  
    #Changes the sim.do exit mode according the system model
    if system_model_description == "scmod":
        
        sim_do_path = testcase_path+"/sim.do"
        sim_file = open(sim_do_path, "a")
        sim_file.write("\nwhen -label end_of_simulation { HeMPS/PE0x0/end_sim_reg == x\"00000000\" } {echo \"End of simulation\" ; quit ;}")
        sim_file.write("\nrun "+str(simul_time)+"ms")
        sim_file.close()
    
    elif system_model_description == "vhdl":
        
        sim_do_path = testcase_path+"/sim.do"
        sim_file = open(sim_do_path, "a")
        sim_file.write("\nwhen -label end_of_simulation { HeMPS/proc(0)/PE/end_sim_reg == x\"00000000\" } {echo \"End of simulation\" ; quit ;}")
        sim_file.write("\nrun "+str(simul_time)+"ms")
        sim_file.close()
        
        
    copyfile(makes_dir+"/make_testcase", testcase_path+"/makefile")
    
    copyfile(makes_dir+"/make_kernel", testcase_path+"/software/makefile")
    
     #Open the file (closing after scope) to append the PAGE_SP_INIT and MEM_SP_INIT value
    make_file_path = testcase_path + "/software/makefile"
    
    lines = []

    lines.append("PAGE_SP_INIT = "+ str((page_size_KB  *  1024) - 1) + "\n")
    lines.append("MEM_SP_INIT  = "+ str((memory_size_KB * 1024) - 1) + "\n")
    
    append_lines_at_end_of_file(make_file_path, lines)
    
    copyfile(makes_dir+"/make_all_apps", testcase_path+"/applications/makefile")
    
    for app_name in apps_list:
        
        make_app_path = testcase_path+"/applications/"+app_name+"/makefile"
        
        copyfile(makes_dir+"/make_app", make_app_path)
        
        line = "PAGE_SP_INIT = "+ str((page_size_KB  *  1024) - 1) + "\n"
        
         #Append the PAGE_SP_INIT value
        append_lines_at_end_of_file(make_app_path, line)
        
def generate_wave(testcase_full_path):
    try:
        os.system("hemps-wave_gen "+testcase_full_path)
    except:
        pass   
#This function copies the input testcase file to the testcase path.
#If the input testcase file is equal to the current testcase file, the copy is suspended. Further, the package generation is not fired 
#The function return if the testcase are equals = True or not equals = False
def copy_testcase_file(testcase_name, input_testcase_file_path):
    
    current_testcase_file_path = testcase_name+"/"+testcase_name+".yaml"
    
    if os.path.isfile(current_testcase_file_path):
    
        #If the file are equals then do nothing
        if filecmp.cmp(input_testcase_file_path, current_testcase_file_path) == True:
            return True
    
        
    copyfile(input_testcase_file_path, current_testcase_file_path)
    return False

def append_lines_at_end_of_file(file_path, lines):
    
    f = open(file_path, "a")
    
    f.writelines(lines)
    
    f.close()
    
#Call of function main, this aproaches enables to call a fucntion before it declaration
main()
