#include "include/libos.h"

/* Shamelessly stolen from K&R C Programing Language Book */

typedef struct header {
    struct header *ptr;
    size_t size;
} header_t __attribute__ ((aligned(MALLOC_ALIGNMENT)));

static header_t base = {.ptr = &base, .size = 0 };
static header_t *freep = &base;

static header_t *morecore(size_t nunits) {
	static header_t *lastp = HEAP_START;
	header_t *p;
	size_t size;
	
	size = align_type(min(align(nunits*sizeof(header_t), HEAP_ALLOCATION_GRAIN),
												((size_t)lastp)-((size_t)HEAP_END)),
										header_t)/sizeof(header_t);

	if(((void*)lastp <= HEAP_END) || size < nunits)
		return NULL;

	p = lastp;
	p->size = size;
	lastp += size;

	return p;
}

void *malloc(size_t nbytes) {
  header_t *p, *prevp;
  unsigned nunits;
  int critical;
  nunits = (nbytes+sizeof(header_t)-1)/sizeof(header_t) + 1;

  critical = enter_critical();

  prevp = freep;
  for (p = prevp->ptr; ; prevp = p, p = p->ptr) {
    if (p->size >= nunits) {
      if (p->size == nunits)
				prevp->ptr = p->ptr;
      else {
				p->size -= nunits;
				p += p->size;
				p->size = nunits;
      }
      freep = prevp;
      leave_critical(critical);
      return (void *)(p+1);
    }
    if (p == freep) {
			if((p = morecore(nunits)) == NULL) {
				leave_critical(critical);
				return NULL;
			}
    }
  }
}

void free(void *ap) {
  header_t *bp, *p;
  int critical;
  bp = (header_t *)ap - 1;

#ifdef DEBUG
  printf("free: Freeing %d bytes\n", bp->size*sizeof(header_t));
#endif

  critical = enter_critical();

  for (p = freep; !(bp > p && bp < p->ptr); p = p->ptr)
    if (p >= p->ptr && (bp > p || bp < p->ptr))
      break; /* freed block at start or end of arena */

  if (bp + bp->size == p->ptr) {
    /* join to upper nbr */
    bp->size += p->ptr->size;
    bp->ptr = p->ptr->ptr;
  } else
    bp->ptr = p->ptr;
  if (p + p->size == bp) {
    /* join to lower nbr */
    p->size += bp->size;
    p->ptr = bp->ptr;
  } else
    p->ptr = bp;
  freep = p;

  leave_critical(critical);
}

void *calloc(size_t qty, size_t type_size){
  size_t *buf, *end, *a;
  size_t size;

  size = qty == 1 ? align_type(type_size, size_t) : align_type(qty*type_size, size_t);
  
  if((buf = malloc(size)))
    for(end = (size_t*)((size_t)buf + size), a = buf ; a < end ; a++)
      *a = 0;
  
  return (void *)buf;
}
