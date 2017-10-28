# HEMPS VERSION - 8.0 - support for RT applications
#
# Distribution:  June 2016
#
# Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
#
# Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
#
# Brief description: Initializes the stack pointer and jumps to main(). Handles the syscall.



        .text
        .align  2
        .globl  entry
        .ent    entry
entry:
  .set noreorder

  la $sp,sp_addr # new initialization

  jal  main
  nop
	
	beq $0, $0, exit
	or  $4, $2, $0
	.end entry

	.globl exit
	.ent exit
	.set noreorder
exit:
	lui $16, 0x2000								# $16 = *HARDWARE_REGISTERS
	la  $17, .RET_MSG							# $17 = *RET_MSG

	# assemble the package
	lw  $8, 320($16) 						# $8 = NET_ADDRESS
	lw  $9, 324($16) 						# $9 = LOADER_NETADDR
	sw  $8, 12($17)							# RET_MSG.SOURCE = $8
	sw  $9, 0($17)							# RET_MSG.TARGET = $9
	ori $8, $0, 5								# $8 = 5 (DMNI_SIZE)
	sw  $4, 0($17)							# RET_MSG.RETURN_CODE = $4

	# send it
	sw	$8, 512($16)						# *DMNI_SIZE = $8
	sw  $17, 528($16)						# *DMNI_ADDR = $17
	ori $8, $0, 1 							# $8 = 1
	sw  $0, 544($16)						# READ -> DMNI_OP
	sw  $8, 560($16) 						# 1 -> DMNI_START

	# commit suicide
	li $8, 0xDEADBEAF 					# MAGIC WORD TO KILL CPU
.L1:
  beq $0, $0, .L1	
  sw $8, 800($16) 						# SET_CPU_KILL
	.end exit

# memory region reserved to assemble termination pkg
.RET_MSG:
	.word 0 											# TARGET
	.word 2												# SIZE
	.word 0x70										# SERVICE
	.word 0												# SOURCE
	.word 0												# RETURN_CODE
  

   .globl SystemCall
   .ent SystemCall
SystemCall:
   .set	noreorder
   
   syscall 
   nop
   jr	$31
   nop
   
   .set reorder
   .end SystemCall


