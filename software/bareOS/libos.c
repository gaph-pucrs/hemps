#include "include/libos.h"

char *utoa(unsigned int i, char *s, int base) {
  char c;
  char *p = s;
  char *q = s;
  int b, shift;

  for(b = base, shift = 0 ; !(b & 1) ; shift++, b >>= 1);
  
  if(b == 1) {
    b = (1 << shift)-1;
    do {
      *q++ = '0' + (i & b);
    } while(i >>= shift);
  } else
    do {
      *q++ = '0' + (i % base);
    } while (i /= base);
  
  for (*q = 0; p <= --q; p++){
    (*p > '9')?(c = *p + 39):(c = *p);
    (*q > '9')?(*p = *q + 39):(*p = *q);
    *q = c;
  }
  
  return s;
}

char *itoa(int i, char *s, int base){
  char c;
  char *p = s;
  char *q = s;

  if (i >= 0)
    return utoa(i, s, base);

  *q++ = '-';
  p++;
  do{
    *q++ = '0' - (i % base);
  } while (i /= base);
  
  for (*q = 0; p <= --q; p++){
    (*p > '9')?(c = *p + 39):(c = *p);
    (*q > '9')?(*p = *q + 39):(*p = *q);
    *q = c;
  }
  
  return s;
}

void *memset(void *dst, int ic, size_t bytes) {
  uint8_t c;
  uint32_t f;

	/* Probably the most assemblish code ever written in C!
	 * This is what you get when you put a processor designer 
	 * to write software */

	c = ic;
	f = (c << 24) | (c << 16) | (c << 8) | c;

	switch(bytes & 3) {
	case 3:
		*((uint8_t*)dst++) = c;
		bytes--;
	case 2:
		*((uint8_t*)dst++) = c;
		bytes--;
	case 1:
		*((uint8_t*)dst++) = c;
		bytes--;
	case 0:
		while(bytes){
			*((uint32_t*)dst++) = f;
			bytes -= 4;
		}
	}
	
  return dst;
}

void *memcpy(void *dst, const void *src, size_t bytes) {

	switch(bytes & 3) {
	case 3:
		*((uint8_t*)dst++) = *((uint8_t*)src++);
		bytes--;
	case 2:
		*((uint8_t*)dst++) = *((uint8_t*)src++);
		bytes--;
	case 1:
		*((uint8_t*)dst++) = *((uint8_t*)src++);
		bytes--;
	case 0:
		switch(((size_t)dst | (size_t)src) & 3) {
		case 1:
		case 3:
			while(bytes){
				*((uint8_t*)dst++) = *((uint8_t*)src++);
				bytes--;
			}
			break;
		case 2:
			while(bytes){
				*((uint16_t*)dst++) = *((uint16_t*)src++);
				bytes-=2;
			}
			break;
		case 0:
			while(bytes){
				*((uint32_t*)dst++) = *((uint32_t*)src++);
				bytes-=4;
			}
		}
	}
	
  return dst;
}

int printf(const char *fmt, ...) {
  va_list ap;
  int ret;

  va_start(ap, fmt);
  ret = vprintf(fmt, ap);
  va_end(ap);

  return ret;
}

int vprintf(const char *fmt, va_list ap){
  char *s;
  int i,j;
  char buf[32];

  while (*fmt){
    if (*fmt != '%')
      putchar(*fmt++);
    else{
      j = 0;
      switch (*++fmt){
      case 'i':
      case 'd':
				i = va_arg(ap, int);
				itoa(i,buf,10);
				puts(buf);
				break;
      case 'u':
				i = va_arg(ap, int);
				utoa(i, buf, 10);
				puts(buf);
				break;
      case 'o':
				i = va_arg(ap, int);
				utoa(i,buf,8);
				puts(buf);
				break;
      case 'p':
				puts("0x");
      case 'X':
				i = va_arg(ap, int);
				utoa(i,buf,16);
				j=0;
				while (buf[j])
					buf[j] = islower(buf[j]) ? buf[j++] - 0x20 : buf[j++];
				puts(buf);
				break;
      case 'x':
				i = va_arg(ap, int);
				itoa(i,buf,16);
				puts(buf);
				break;
      case 'c':
				putchar(va_arg(ap, int));
				break;
      case 's':
				s = va_arg(ap, char*);
				if (!s) s = "(null)";
				puts(s);
				break;
      case '%' :
				putchar('%');
				break;
      }
      fmt++;
    }
  }

  return 0;
}
