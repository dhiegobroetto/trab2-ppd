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

Ainda é necessário adicionar os JARs externos do glassfish no Eclipse.

Para isso, basta clicar com o botão direito sob o projeto (Alt + Enter):

* Clicar em _Java Build Path_
* Clicar em _Add External JARs_
* Selecionar os JARS **appserv-rt.jar**, **gf-client.jar** e **javaee.jar**. Todos os três se encontram no diretório:
`<diretório do glassfish>/lib/`

# Instalação do projeto

Faça um clone do projeto em alguma pasta da sua máquina:

`git clone https://github.com/dhiegobroetto/trab2-ppd.git`

Crie uma pasta _bin_ no diretório raíz do projeto.

# Compilar e executar

Com as filas importadas, agora podemos compilar o projeto.

Execute na pasta raíz do projeto o comando:

`javac -d bin -cp .:/<diretório do glassfish>/lib/gf-client.jar src/*.java`

Para executar cada arquivo, basta executar:

`java -cp .:./bin:/<diretório do glassfish>/lib/gf-client.jar <nome da classe>`

# Especificações:

O objetivo deste trabalho é praticar programação paralela usando middleware orientado a mensagens JMS e realizar análise de desempenho em um cluster de computadores. O trabalho inclui a comparação desta implementação com a implementação do primeiro trabalho (baseada em Request-Response com Java RMI).

A ideia agora é calcular o **m**, que será uma variável que iremos utilizar para definir a quantidade de mensagens que irão ser enviados aos demais escravos, anteriormente utilizávamos a quantidade de escravos ativos no sistema.

Adotamos MapMessage como a estrutura de mensagens que será enviada às filas, com as informações:
```java
byte[] ciphertext, byte[] knowntext, long initialwordindex, long finalwordindex, int attackNumber;
```

O cabeçalho das mensagens dos escravos para o master, através da fila _GuessesQueue_, haverá o _attackNumber_. Isso será feito para o mestre saber onde direcionar o guess encontrado.
