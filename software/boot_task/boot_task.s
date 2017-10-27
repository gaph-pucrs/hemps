## HEMPS VERSION - 8.0 - support for RT applications
##
## Distribution:  June 2016
##
## Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
##
## Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
##
## Brief description: Initializes the stack pointer and jumps to main(). Handles the syscall.



        .text
        .align  2
        .globl  entry
        .ent    entry
entry:
  .set noreorder

  li $sp,sp_addr ; new initialization

  jal  main
  nop
	
	beq $zero, $zero, exit
	or  $a0, $v0, $zero
	.end entry

	.globl exit
	.ent exit
	.set noreorder
exit:
	lui $s0, 0x2000								; $s0 = *HARDWARE_REGISTERS
	la  $s1, .RET_MSG							; $s1 = *RET_MSG

	;; assemble the package
	lw  $t0, 320($s0) 						; $t0 = NET_ADDRESS
	lw  $t1, 324($s0) 						; $t1 = LOADER_NETADDR
	sw  $t0, 12($s1)							; RET_MSG.SOURCE = $t0
	sw  $t1, 0($s1)								; RET_MSG.TARGET = $t1
	ori $t0, $zero, 5							; $t0 = 5 (DMNI_SIZE)
	sw  $a0, 0($s1)								; RET_MSG.RETURN_CODE = $a0

	;; send it
	sw	$t0, 512($s0)							; *DMNI_SIZE = $t0
	sw  $s1, 528($s0)							; *DMNI_ADDR = $s1
	ori $t0, $zero, 1 						; $t0 = 1
	sw  $zero, 544($s0)						; READ -> DMNI_OP
	sw  $t0, 560($s0) 						; 1 -> DMNI_START

	;; commit suicide
	li $t0, 0xDEADBEAF 						; MAGIC WORD TO KILL CPU
.L1:
  beq $zero, $zero, .L1	
  sw $t0, 800($s0) 							; SET_CPU_KILL
	.end exit

; memory region reserved to assemble termination pkg
.RET_MSG:
	.word 0 											; TARGET
	.word 2												; SIZE
	.word 0x70										; SERVICE
	.word 0												; SOURCE
	.word 0												; RETURN_CODE
  
###################################################

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


