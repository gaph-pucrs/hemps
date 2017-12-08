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

union ptr {
  const void *v;
  uint8_t *uint8;
  uint16_t *uint16;
  uint32_t *uint32;
};


void *memset(void *dst, int ic, size_t bytes) {
  uint8_t c;
  uint32_t f;

  union ptr d = {.v = dst};

  /* Probably the most assemblish code ever written in C!
   * This is what you get when you put a processor designer
   * to write software */

  c = ic;

  switch(bytes & 3) {
  case 3:
    *(d.uint8++) = c;
    bytes--;
  case 2:
    *(d.uint8++) = c;
    bytes--;
  case 1:
    *(d.uint8++) = c;
    bytes--;
  case 0:
    f = (c << 24) | (c << 16) | (c << 8) | c;
    while(bytes){
      *(d.uint32++) = f;
      bytes -= 4;
    }
  }

  return dst;
}

void *memcpy(void *dst, const void *src, size_t bytes) {

  union ptr d = {.v = dst} , s = {.v = src};

  while(bytes) {
    switch(((size_t)d.v | (size_t)s.v | bytes) & 3) {
    case 0:
      *(d.uint32++) = *(s.uint32++);
      bytes-=4;
      break;
    case 2:
      *(d.uint16++) = *(s.uint16++);
      bytes-=2;
      break;
    case 1:
    case 3:
      *(d.uint8++) = *(s.uint8++);
      bytes--;
      break;
    }
  }

  return dst;
}

int strcmp(const char *a, const char *b) {
  while(*a && *b && *a == *b) a++, b++;

  return *a - *b;
}

size_t strlen(const char *a) {
  size_t ret;

  for(ret = 0 ; *a ; a++, ret++);

  return ret;
}

void panic(const char *fmt, ...) {
  va_list ap;
  int ret;

  puts("PANIC: ");

  va_start(ap, fmt);
  ret = vprintf(fmt, ap);
  va_end(ap);

  putchar('\n');

  exit(-1);
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
