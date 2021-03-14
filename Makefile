.PHONY: build clean
 
build: Main.class
 
run:
	java Main $(input) $(base)
 
Main.class: Main.java
	javac $^
clean:
	rm Main.class