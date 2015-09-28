.PHONY: clean tags default

default: grader

grader : main.o common.o Config.o Grader.o Comm.o Grade.o Parser.o Constants.o grade.pb.o
	g++ -g -o ../grader main.o common.o Config.o Grader.o Comm.o Grade.o Parser.o Constants.o grade.pb.o -lprotobuf -lpthread -Wall
	./mk-syscall-table
	gcc box.c -std=c99 -O2 --static -o -Wall -o ../sandbox
	gcc unbuffered_stdout.c -c -O2 -Wall -o ../lib/unbuffered_stdout.o 
main.o : main.cpp
	g++ -g -c main.cpp -Wall
common.o : common.h common.cpp
	g++ -g -c common.cpp -Wall
Config.o : Config.h Config.cpp
	g++ -g -c Config.cpp -Wall
Grader.o : Grader.h Grader.cpp
	g++ -g -c Grader.cpp -Wall
Comm.o : Comm.h Comm.cpp
	g++ -g -c Comm.cpp -Wall
Grade.o : Grade.h Grade.cpp
	g++ -g -c Grade.cpp -Wall
Parser.o : Parser.h Parser.cpp
	g++ -g -c Parser.cpp -Wall
Constants.o : Constants.h Constants.cpp
	g++ -g -c Constants.cpp -Wall
grade.pb.o : grade.pb.cc
	g++ -g -c grade.pb.cc -Wall
	
clean:
	rm -f *.o

tags:
	ctags -R

grade.pb:
	protoc -I=. --cpp_out=. grade.proto
