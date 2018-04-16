from loremipsum import get_paragraphs

for i in range(500):
	f = open("text" + str(i) + ".txt", "w+")
	text = get_paragraphs(10 * (i+1))
	f.write(" ".join(text))
	f.close
