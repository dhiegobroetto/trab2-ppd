# Trabalho 2 de PPD
Ataque de Dicionário em Mensagem Criptografada com Middleware Orientado a Mensagens.

## Pré requisitos:
### Glassfish 5
Como instalar:
No linux:

`wget http://download.oracle.com/glassfish/5.0/release/glassfish-5.0.zip`

E descompacte o zip em um diretório qualquer.

Entrar na pasta bin do glassfish e executar:

`./asadmin start-domain`

Para iniciar o servidor do glassfish (isso pode levar alguns minutos).

É necessário importar o arquivo XML de configuração para as filas. Para isso basta executar, ainda na pasta bin do glassfish:

`./asadmin add-resources <diretório do arquivo>/<nome do arquivo>.xml`

# Compilar e executar

Com as filas importadas, agora podemos compilar o projeto.

Execute na pasta raíz do projeto o comando:

`javac -d bin -cp .:/<diretório do glassfish>/lib/gf-client.jar src/*.java`

No nosso projeto fica:

`javac -d bin -cp .:/<diretório do glassfish>/lib/gf-client.jar src/br/inf/ufes/ppd/*.java src/br/inf/ufes/ppd/client/*.java src/br/inf/ufes/ppd/master/*.java src/br/inf/ufes/ppd/model/*.java src/br/inf/ufes/ppd/slave/*.java`

Para executar cada arquivo, basta executar:

`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar <caminho da classe>`

No nosso projeto fica, por exemplo:

`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar br.inf.ufes.ppd.master.MasterExecute`

Os parâmetros para execução do código fica:

`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar br.inf.ufes.ppd.master.MasterExecute <dicionario> <m>`
`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar br.inf.ufes.ppd.slave.SlaveExecute <host> <dicionario> <slaveName>`
`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar br.inf.ufes.ppd.client.ClientExecute <host> <knownword> <cipher>`

# Especificações:

O objetivo deste trabalho é praticar programação paralela usando middleware orientado a mensagens JMS e realizar análise de desempenho em um cluster de computadores. O trabalho inclui a comparação desta implementação com a implementação do primeiro trabalho (baseada em Request-Response com Java RMI).

A ideia agora é calcular o **m**, que será uma variável que iremos utilizar para definir a quantidade de mensagens que irão ser enviados aos demais escravos, anteriormente utilizávamos a quantidade de escravos ativos no sistema.

Adotamos MapMessage como a estrutura de mensagens que será enviada às filas, com as informações:
```java
byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber;
```

O cabeçalho das mensagens dos escravos para o master, através da fila _GuessesQueue_, haverá o _attackNumber_. Isso será feito para o mestre saber onde direcionar o guess encontrado.
