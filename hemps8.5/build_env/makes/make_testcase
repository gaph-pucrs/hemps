YAML_FILE=$(shell ls *.yaml)
TC_NAME=$(notdir ${CURDIR})

default: all

all: banner apps kernel hw

update:
	@cd ..; hemps-gen $(TC_NAME).yaml 1; cd $(TC_NAME); make clean; make all; cd ..;
	
banner:
	@python build/banner.py

apps:
	@python build/app_builder.py $(YAML_FILE)
	
kernel:
	@python build/kernel_builder.py $(YAML_FILE)
	
hw:
	@python build/hw_builder.py $(YAML_FILE)
	
clean-apps:
	@make clean -C applications/
	@rm -rf log/*

clean-kernel:
	@make clean -C software/
	@rm -rf log/*
	
clean-hw:
	@make clean -C hardware/
	@rm -rf log/*
	
clean: clean-apps clean-kernel clean-hw
	@rm -rf *.txt
	@rm -f HeMPS
	@rm -rf log/*
