*************************************************
HEMPS - Hermes Multiprocessor System on Chip	*	
												*
Version: 8.0									*
*************************************************


*************************************************
TODO:		          			

1. GERACAO DE RAM VHDL:
	a. Observar o comportamento da interface da ram gerada pelo ram_generator( pode ser tanto a ram_master ou ram plasma)
	b. Implemtar esse comportamento dentro do arquivo memory/ram.vhd
	c. No mesmo arquivo fazer o carregamento automático das ram, lendo os links simbolicos da pasta pe_ram (da mesma forma que é feito em SystemC)
	d. Validar

2. APPLICATION:
	a. Passar a pasta contendo as aplicações pelo qual se deseja executar
	b. Se não for passado pasta alguma, pegar automaticamente do HEMPS_PATH

3. GERAÇÃO DE REPÓSITÓRIO:
	a. Ler o arquivo .yaml dentro de cada app para saber as dependencias de cada tarefa e o load de comunicação e computação
	b. Preencher essa informação nos 21 campos do repositório (1 de load de computação e os 20 demais, de comunicação)
	c. O kernel já está pronto, não precisa alterar.

4. MIGRACAO DE TAREFAS COM RECLUSTERING
	a. Implementar suporte ao mestre de migrar uma terafa que esta fora do seu cluster
**************************************************
