import sys,tty,termios,random

x=random.randint(0,3)
y=random.randint(0,3)

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
	for i in range(4):
		k=0
		Matrx = [0 for x in range(4)]
		for j in range(4):
			if(Matrix[i][j]==0):
				return 0
			Matrx[k]=Matrix[i][j]
			k=k+1
		#Matrx.sort();
		for l in range(3):
			if(Matrx[l]==Matrx[l+1]):
				return 0

	for i in range(4):
                k=0
                Matrx = [0 for x in range(4)]
                for j in range(4):
                        Matrx[k]=Matrix[j][i]
                        k=k+1
                #Matrx.sort();
                for l in range(3):
                        if(Matrx[l]==Matrx[l+1]):
                                return 0

	return isFull

def left(Matrix):
        prev=-1
        cur=-1
        for i in range(4):  
                ini=0
                inir=0
                prev=-1
		for j in range(4):
                        if (Matrix[i][j]!=0 and prev==-1):
                                prev=j
                        elif(prev!=-1 and Matrix[i][j]!=0 and Matrix[i][j]==Matrix[i][prev]):
                                Matrix[i][prev]=2*Matrix[i][prev]
                                Matrix[i][j]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[i][j]!=0 and Matrix[i][j]!=Matrix[i][prev]):
                                prev=j

		while (ini<=3):
                        if(Matrix[3-i][ini]!=0):
                                Matrix[3-i][inir]=Matrix[3-i][ini]
                                if(ini!=inir):
                                        Matrix[3-i][ini]=0
                                inir=inir+1
                        ini=ini+1

def right(Matrix):
	prev=-1
	cur=-1
	for i in range(4):
		ini=3
		inir=3
		prev=-1
		
		for j in range(4):
                        if (Matrix[3-i][3-j]!=0 and prev==-1):
                                prev=3-j
                        elif(prev!=-1 and Matrix[3-i][3-j]!=0 and Matrix[3-i][3-j]==Matrix[3-i][prev]):
                                Matrix[3-i][prev]=2*Matrix[3-i][prev]
                                Matrix[3-i][3-j]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[3-i][3-j]!=0 and Matrix[3-i][3-j]!=Matrix[3-i][prev]):
                                prev=3-j

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
        for i in range(4):
                ini=3
                inir=3
                prev=-1
                for j in range(4):
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
        for i in range(4):
                ini=0
                inir=0
                prev=-1
                for j in range(4):
                        if(Matrix[3-j][3-i]!=0 and prev==-1):
                                prev=3-j
                        elif(prev!=-1 and Matrix[3-j][3-i]!=0 and Matrix[3-j][3-i]==Matrix[prev][3-i]):
                                Matrix[3-j][3-i]=2*Matrix[prev][3-i]
                                Matrix[prev][3-i]=0
                                prev=-1
                        elif(prev!=-1 and Matrix[3-j][3-i]!=0 and Matrix[3-j][3-i]!=Matrix[prev][3-i]):
                                prev=3-j
		while (ini<=3):
                        if(Matrix[ini][3-i]!=0):
                                Matrix[inir][3-i]=Matrix[ini][3-i]
                                if(ini!=inir):
                                        Matrix[ini][3-i]=0
                                inir=inir+1
                        ini=ini+1

def printArray(Matrix):
         sys.stderr.write("\x1b[2J\x1b[H")
	 print "----------------------------",
	 print

	 for i in range(4):
                for j in range(4):
                        if(Matrix[i][j]==0):
				print str(" ").rjust(4),
			else:
				print str(Matrix[i][j]).rjust(4),
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
	x=random.randint(0,3)
	if dir=="up":
		if Matrix[3][x]==0:
                                Matrix[3][x]=2
				printArrayC(Matrix,3,x)
				return
		for i in range(4):
			if Matrix[3][i]==0:
				Matrix[3][i]=2
				printArrayC(Matrix,3,i)
				break
	
	elif dir=="down":   
                if Matrix[0][x]==0:
                                Matrix[0][x]=2
                                printArrayC(Matrix,0,x)
				return
		for i in range(4):
                        if Matrix[0][i]==0:
                                Matrix[0][i]=2
				printArrayC(Matrix,0,i)
				break

	elif dir=="left":
		if Matrix[x][3]==0:
                                Matrix[x][3]=2
                                printArrayC(Matrix,x,3)
				return
                for i in range(4):
                        if Matrix[i][3]==0:
                                Matrix[i][3]=2
				printArrayC(Matrix,i,3)
				break

	elif dir=="right":
		if Matrix[x][0]==0:
                                Matrix[x][0]=2
                                printArrayC(Matrix,x,0)
				return
                for i in range(4):
                        if Matrix[i][0]==0:
                                Matrix[i][0]=2
				printArrayC(Matrix,i,0)
				break	

def printArrayC(Matrix,x,y):
	 sys.stderr.write("\x1b[2J\x1b[H")
	 print "----------------------------",
         print	
         for i in range(4):
                for j in range(4):
                        if x==i and y==j:
                                print '\033[84m'+"\033[1m" + str(Matrix[i][j]).rjust(4) + '\033[0m',
                        else:
				if(Matrix[i][j]==0):
                                	print str(" ").rjust(4),
                        	else:
					print giveColor(Matrix[i][j]), 
                        print "|",
                print
	 print "----------------------------",
         print

def giveColor(x):
	
	if x==2:
		return '\033[95m'+str(x).rjust(4)+'\033[0m'
	elif x==4:
                return '\033[94m'+str(x).rjust(4)+'\033[0m'
	elif x==8:
                return '\033[96m'+str(x).rjust(4)+'\033[0m'
	elif x==16:
                return '\033[93m'+str(x).rjust(4)+'\033[0m'
	elif x==32:
                return '\033[92m'+str(x).rjust(4)+'\033[0m'
	elif x==64:
                return '\033[90m'+str(x).rjust(4)+'\033[0m'
	elif x==128:
                return '\033[89m'+str(x).rjust(4)+'\033[0m'
	elif x==256:
                return '\033[95m'+str(x).rjust(4)+'\033[0m'
	elif x==512:
                return '\033[94m'+str(x).rjust(4)+'\033[0m'
	elif x==1024:
                return '\033[96m'+str(x).rjust(4)+'\033[0m'
	elif x==2048:
                return '\033[93m'+str(x).rjust(4)+'\033[0m'

def hasWon(Matrix):
	 for i in range(4):
                for j in range(4):
			if Matrix[i][j]==2048:
				return 1
	 return 0


def main():

	x=random.randint(0,3)
	y=random.randint(0,3)
	Matrix = [[0 for x in range(4)] for x in range(4)]
	Matrix[x][y]=2;	
	x=random.randint(0,3)
        y=random.randint(0,3)
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
