import sys,tty,termios,random

length=3
size=4

x=random.randint(0,length)
y=random.randint(0,length)

class _Getch:
    def __call__(self):
            fd = sys.stdin.fileno()
            old_settings = termios.tcgetattr(fd)
            try:
                tty.setraw(sys.stdin.fileno())
                ch = sys.stdin.read(3)
            finally:
                termios.tcsetattr(fd, termios.TCSADRAIN, old_settings)
            return ch

def isArrayFull(Matrix):
	isFull=1
	for i in range(size):
		k=0
		Matrx = [0 for x in range(size)]
		for j in range(size):
			if(Matrix[i][j]==0):
				return 0
			Matrx[k]=Matrix[i][j]
			k=k+1
		#Matrx.sort();
		for l in range(length):
			if(Matrx[l]==Matrx[l+1]):
				return 0

	for i in range(size):
                k=0
                Matrx = [0 for x in range(size)]
                for j in range(size):
                        Matrx[k]=Matrix[j][i]
                        k=k+1
                #Matrx.sort();
                for l in range(length):
                        if(Matrx[l]==Matrx[l+1]):
                                return 0

	return isFull

def left(Matrix):
        prev=-1
        cur=-1
        for i in range(size):  
                ini=0
                inir=0
                prev=-1
		for j in range(size):
                        if (Matrix[i][j]!=0 and prev==-1):
                                prev=j
                        elif(prev!=-1 and Matrix[i][j]!=0 and Matrix[i][j]==Matrix[i][prev]):
                                Matrix[i][prev]=2*Matrix[i][prev]
                                Matrix[i][j]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[i][j]!=0 and Matrix[i][j]!=Matrix[i][prev]):
                                prev=j

		while (ini<=length):
                        if(Matrix[length-i][ini]!=0):
                                Matrix[length-i][inir]=Matrix[length-i][ini]
                                if(ini!=inir):
                                        Matrix[length-i][ini]=0
                                inir=inir+1
                        ini=ini+1

def right(Matrix):
	prev=-1
	cur=-1
	for i in range(size):
		ini=length
		inir=length
		prev=-1
		
		for j in range(size):
                        if (Matrix[length-i][length-j]!=0 and prev==-1):
                                prev=length-j
                        elif(prev!=-1 and Matrix[length-i][length-j]!=0 and Matrix[length-i][length-j]==Matrix[length-i][prev]):
                                Matrix[length-i][prev]=2*Matrix[length-i][prev]
                                Matrix[length-i][length-j]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[length-i][length-j]!=0 and Matrix[length-i][length-j]!=Matrix[length-i][prev]):
                                prev=length-j

		while (ini>=0):
                        if(Matrix[i][ini]!=0):
                                Matrix[i][inir]=Matrix[i][ini]
                                if(ini!=inir):
                                        Matrix[i][ini]=0
                                inir=inir-1
                        ini=ini-1

def down(Matrix):
        prev=-1
        cur=-1
        for i in range(size):
                ini=length
                inir=length
                prev=-1
                for j in range(size):
                        if (Matrix[j][i]!=0 and prev==-1):
                                prev=j
                        elif(prev!=-1 and Matrix[j][i]!=0 and Matrix[j][i]==Matrix[prev][i]):
                                Matrix[j][i]=2*Matrix[prev][i]
                                Matrix[prev][i]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[j][i]!=0 and Matrix[j][i]!=Matrix[prev][i]):
                                prev=j
		while (ini>=0):
                        if(Matrix[ini][i]!=0):
                                Matrix[inir][i]=Matrix[ini][i]
                                if(ini!=inir):
                                        Matrix[ini][i]=0
                                inir=inir-1
                        ini=ini-1


def up(Matrix):
        prev=-1
        cur=-1
        for i in range(size):
                ini=0
                inir=0
                prev=-1
                for j in range(size):
                        if(Matrix[length-j][length-i]!=0 and prev==-1):
                                prev=length-j
                        elif(prev!=-1 and Matrix[length-j][length-i]!=0 and Matrix[length-j][length-i]==Matrix[prev][length-i]):
                                Matrix[length-j][length-i]=2*Matrix[prev][length-i]
                                Matrix[prev][length-i]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[length-j][length-i]!=0 and Matrix[length-j][length-i]!=Matrix[prev][length-i]):
                                prev=length-j
		while (ini<=length):
                        if(Matrix[ini][length-i]!=0):
                                Matrix[inir][length-i]=Matrix[ini][length-i]
                                if(ini!=inir):
                                        Matrix[ini][length-i]=0
                                inir=inir+1
                        ini=ini+1

def printArray(Matrix):
         sys.stderr.write("\x1b[2J\x1b[H")
	 print "----------------------------",
	 print

	 for i in range(size):
                for j in range(size):
                        if(Matrix[i][j]==0):
				print str(" ").rjust(size),
			else:
				print str(Matrix[i][j]).rjust(size),
			print "|",
                print
	 print "----------------------------",
         print	

def get(Matrix):
        inkey = _Getch()
        while(1):
                k=inkey()
                if k!='':break
        if k=='\x1b[A':
                up(Matrix)
       		putNewNumber(Matrix,"up") 
	elif k=='\x1b[B':
                down(Matrix)
		putNewNumber(Matrix,"down")
        elif k=='\x1b[C':
                right(Matrix)
		putNewNumber(Matrix,"right")
        elif k=='\x1b[D':
                left(Matrix)
		putNewNumber(Matrix,"left")
        else:
                print "not an arrow key!"
		putNewNumber(Matrix,"left")

def putNewNumber(Matrix,dir):
	x=random.randint(0,length)
	if dir=="up":
		if Matrix[length][x]==0:
                                Matrix[length][x]=2
				printArrayC(Matrix,length,x)
				return
		for i in range(size):
			if Matrix[length][i]==0:
				Matrix[length][i]=2
				printArrayC(Matrix,length,i)
				break
	
	elif dir=="down":   
                if Matrix[0][x]==0:
                                Matrix[0][x]=2
                                printArrayC(Matrix,0,x)
				return
		for i in range(size):
                        if Matrix[0][i]==0:
                                Matrix[0][i]=2
				printArrayC(Matrix,0,i)
				break

	elif dir=="left":
		if Matrix[x][length]==0:
                                Matrix[x][length]=2
                                printArrayC(Matrix,x,length)
				return
                for i in range(size):
                        if Matrix[i][length]==0:
                                Matrix[i][length]=2
				printArrayC(Matrix,i,length)
				break

	elif dir=="right":
		if Matrix[x][0]==0:
                                Matrix[x][0]=2
                                printArrayC(Matrix,x,0)
				return
                for i in range(size):
                        if Matrix[i][0]==0:
                                Matrix[i][0]=2
				printArrayC(Matrix,i,0)
				break	

def printArrayC(Matrix,x,y):
	 sys.stderr.write("\x1b[2J\x1b[H")
	 print "----------------------------",
         print	
         for i in range(size):
                for j in range(size):
                        if x==i and y==j:
                                print '\033[84m'+"\033[1m" + str(Matrix[i][j]).rjust(size) + '\033[0m',
                        else:
				if(Matrix[i][j]==0):
                                	print str(" ").rjust(size),
                        	else:
					print giveColor(Matrix[i][j]), 
                        print "|",
                print
	 print "----------------------------",
         print

def giveColor(x):
	
	if x==2:
		return '\033[95m'+str(x).rjust(size)+'\033[0m'
	elif x==4:
                return '\033[94m'+str(x).rjust(size)+'\033[0m'
	elif x==8:
                return '\033[96m'+str(x).rjust(size)+'\033[0m'
	elif x==16:
                return '\033[93m'+str(x).rjust(size)+'\033[0m'
	elif x==32:
                return '\033[92m'+str(x).rjust(size)+'\033[0m'
	elif x==64:
                return '\033[90m'+str(x).rjust(size)+'\033[0m'
	elif x==128:
                return '\033[89m'+str(x).rjust(size)+'\033[0m'
	elif x==256:
                return '\033[95m'+str(x).rjust(size)+'\033[0m'
	elif x==512:
                return '\033[94m'+str(x).rjust(size)+'\033[0m'
	elif x==1024:
                return '\033[96m'+str(x).rjust(size)+'\033[0m'
	elif x==2048:
                return '\033[93m'+str(x).rjust(size)+'\033[0m'

def hasWon(Matrix):
	 for i in range(size):
                for j in range(size):
			if Matrix[i][j]==2048:
				return 1
	 return 0


def main():

	x=random.randint(0,length)
	y=random.randint(0,length)
	Matrix = [[0 for x in range(size)] for x in range(size)]
	Matrix[x][y]=2	
	x=random.randint(0,length)
        y=random.randint(0,length)
	Matrix[x][y]=2
	printArray(Matrix)
        while (hasWon(Matrix)==0):
                get(Matrix)
		if isArrayFull(Matrix)==1:
			print "Game Over!!!!!"
			sys.exit(0)

	print "You won!!!!!!!!!"
	sys.exit(0)

if __name__=='__main__':
        main()
