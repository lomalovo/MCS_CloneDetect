CFLAGS = -std=c++17 -Wall -Wextra -Werror -Iinclude -O3 -g
.PHONY: all clean

all: obj clonedetect
clean:
	rm -rf obj clonedetect
obj:
	mkdir -p obj

clonedetect: obj/parseTokens.o obj/verifySim.o obj/cloneDetect.o obj/main.o
	g++ $(CFLAGS) obj/parseTokens.o obj/verifySim.o obj/cloneDetect.o obj/main.o -o clonedetect

obj/parseTokens.o: core_cpp/parseTokens.cpp include/parseTokens.h
	g++ $(CFLAGS) -c core_cpp/parseTokens.cpp -o obj/parseTokens.o

obj/verifySim.o: core_cpp/verifySim.cpp include/verifySim.h
	g++ $(CFLAGS) -c core_cpp/verifySim.cpp -o obj/verifySim.o

obj/cloneDetect.o: core_cpp/cloneDetect.cpp include/cloneDetect.h
	g++ $(CFLAGS) -c core_cpp/cloneDetect.cpp -o obj/cloneDetect.o

obj/main.o: core_cpp/main.cpp
	g++ $(CFLAGS) -c core_cpp/main.cpp -o obj/main.o