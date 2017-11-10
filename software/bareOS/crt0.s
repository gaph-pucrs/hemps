	.section .text.init
	.global _entry
	.ent _entry
	.set noreorder
_entry:
	la $sp, _stack
	la $gp, _gp
	la $4, _bss_start
	la $5, _bss_end
.clr_bss:
	beq $4, $5, .clr_bss_done
	nop
	sw $0, 0($4)
	beq $0, $0, .clr_bss
	addiu $4, $4, 4
.clr_bss_done:
	jal main
	nop
	j exit
	or $4, $2, $0
	.set reorder
	.end _entry

	.section data
	.global _mem_end_ptr
_mem_end_ptr:	.word _stack
