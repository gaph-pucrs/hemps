	.section .text.init
	.global _entry
	.ent _entry
	.set noreorder
_entry:
	la $sp, _stack
	addiu $sp, $sp, 3
	li $2, -4
	and $sp, $sp, $2
	la $gp, _gp
	la $4, _bss_start
	la $5, _bss_end
.clr_bss:
	beq $4, $5, .clr_bss_done
	addiu $4, $4, 4
	beq $0, $0, .clr_bss
	sw $0, -4($4)
.clr_bss_done:
	jal main
	nop
	j exit
	or $4, $2, $0
	.set reorder
	.end _entry
