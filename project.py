






def year():
    nome = input("Digite seu nome: ")

    idade = input("Digite sua idade: ")

    print("seu nome e: ", nome)
    print("sua idade e: ", idade)

    if int(idade) == 18 or int(idade) > 18:
        print("já pode ser preso otário, kkkkkkkk")
    else:
        print("espera mais um pouquinho, que daqui a pouco pode ser preso kkkkk")



def senhas() :
    #limite de senhas == 3
    tentativas = 3

    while tentativas > 0:
        senha = input("Digite a senha: ")

        if senha == "123":
            print("senha correta")
            break

        else :
            tentativas = tentativas - 1 
            print("senha incorreta, voce tem apenas " , tentativas , " tentativas restantes" ) 
        

def list_comidas() :
    comidas = ["arroz" , "feijão" , "bife" , "camarão"]

    for x in comidas :
        print("Eu gosto de " + x)




print("1 para escolher o modulo de year")
print("2 para escolher o modulo de senhas")
print("3 para escolher o módulo de comidas")

escolhas = input("Escolha o número de seu modulo : ")

if escolhas == "1":
    year()
elif escolhas == "2":
    senhas()
elif escolhas == "3":
    list_comidas()
else:
    exit



