#!/usr/bin/python3
import sys
import os

# apos executar o comando make na pasta

vetor=[]
ref_arquivo= open(sys.argv[1],'r')
i=0;
for linha in ref_arquivo:
    valores = linha.split()
    vetor.append(linha)
    # print 'data_dma_code[',i,']= 0x',linha,';'
    # print ('data_dma_code['+i+']='+valores[1]+';')
    # i=i+1;

ref_arquivo.close()

print('const unsigned int {}[] = {}'.format(sys.argv[2], '{')) 

for j in range(0,len(vetor)):
    teste = str(vetor[j])
    final = ''
    for i in teste:
       	if i != '\n':
            final += i
    print('\t0x'+final, end='')
    if j != len(vetor)-1:
        print(',')
    else:
        print('};')

