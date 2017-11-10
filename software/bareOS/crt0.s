	.section .text.init
	.global _entry
	.ent _entry
	.set noreorder
_entry:
	la $sp, _stack
	la $gp, _gp
	la $a0, _bss_start
	la $a1, _bss_end
.clr_bss:
	beq a0, a1, .clr_bss_done
	nop
	sw zero, 0($a0)
	beq $zero, $zero, .clr_bss
	addiu a0, a0, 4
.clr_bss_done:
	jal main
	not
	j exit
	or $a0, $v0, $zero
	.set reorder
	.end _entry
