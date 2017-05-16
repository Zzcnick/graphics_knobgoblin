all: Picture.java
	javac Picture.java && java Picture test.scr && display robot.png

run2: Picture.java
	make && echo "Running..." && java Picture && make png && rm out.ppm && echo "Saved as out.png" && display out.png

script:
	make && echo "Running script..." && java Picture test.scr && make png && rm out.ppm && echo "Saved as out.png" && display out.png

clean: 
	rm *.class *.ppm *~ *.jpg *.png

jpg: out.ppm
	java Picture; \
	convert out.ppm out.jpg

png: out.ppm
	java Picture; \
	convert out.ppm out.png
