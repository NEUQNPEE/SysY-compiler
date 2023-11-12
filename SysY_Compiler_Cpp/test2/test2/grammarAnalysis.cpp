#include"wordAnalysis.h"
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <sstream>
#include <vector>
using namespace std;
vector<string> Key;
vector<string> Value;
int Number=0;
void Write(string str1, string str2)
{
    ofstream out("output.txt", ios::app);
    out << str2 << " " << str1 << endl;
    out.close();
}
void BType();
void LVal();
void NUmber();
void PrimaryExp();
void FuncRParams();
void UnaryOp();
void UnaryExp();
void MulExp();
void AddExp();
void RelExp();
void EqExp();
void LAndExp();
void LOrExp();
void Cond();
void ConstExp();
void Exp();
void ConstInitVal();
void ConstDef();
void ConstDecl();
void InitVal();
void VarDef();
void VarDecl();
void Decl();
void FuncType();
void FuncFParam();
void FuncFParams();
void Stmt();
void BlockItem();
void Block();
void FuncDef();
void MainFuncDef();
void CompUnit();
void analysisGrammar();
int main()
{
	init();
	readtext();
	copy(Key, Value);
	ofstream ofs("output.txt", ios::trunc);
	ofs.close();
	analysisGrammar();
	return 0;
}
void BType()
{
	Write(Key[Number], Value[Number]);
	Number++;
}
void LVal()
{
	Write(Key[Number], Value[Number]);
	Number++;
	if (Value[Number] == "LBRACK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		while (true)
		{
			if (Value[Number] == "LBRACK")
			{
				Write(Key[Number], Value[Number]);
				Number++;
			}
			else if (Value[Number] == "RBRACK")
			{
				if (Value[Number + 1] == "LBRACK")
				{
					Write(Key[Number], Value[Number]);
					Number++;
				}
				else
				{
					Write(Key[Number], Value[Number]);
					Number++;
					break;
				}
			}
			else
			{
				Exp();
			}
		}
	}
	else
	{

	}
	ofstream Output("output.txt", ios::app);
	Output << "<LVal>" << endl;
	Output.close();
}
void NUmber()
{
	Write(Key[Number], Value[Number]);
	Number++;
	ofstream Output("output.txt", ios::app);
	Output << "<Number>" << endl;
	Output.close();
}
void PrimaryExp()
{
	if (Value[Number] == "LPARENT")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Exp();
		Write(Key[Number], Value[Number]);
		Number++;
	}
	else if (Value[Number] == "IDENFR")
	{
		LVal();
	}
	else
	{
		NUmber();
	}
	ofstream Output("output.txt", ios::app);
	Output << "<PrimaryExp>" << endl;
	Output.close();
}
void FuncRParams()
{
	int n = 1;
	while (n)
	{
		if (Value[Number] == "RPARENT")
		{
			n--;
			if (n != 0)
			{
				Write(Key[Number], Value[Number]);
				Number++;
			}
		}
		else if (Value[Number] == "RPARENT")
		{
			n++;
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else if (Value[Number] == "COMMA")
		{
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else
		{
			Exp();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<FuncRParams>" << endl;
	Output.close();
}
void UnaryOp()
{
	Write(Key[Number], Value[Number]);
	Number++;
	ofstream Output("output.txt", ios::app);
	Output << "<UnaryOp>" << endl;
	Output.close();
}
void UnaryExp()
{
	if (Value[Number] == "LPARENT"||Value[Number] == "INTCON")
	{
		PrimaryExp();
	}
	else if (Value[Number] == "IDENFR")
	{
		if (Value[Number + 1] == "LPARENT")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			Write(Key[Number], Value[Number]);
			Number++;
			if (Value[Number] == "RPARENT")
			{
				Write(Key[Number], Value[Number]);
				Number++;
			}
			else
			{
				FuncRParams();
				Write(Key[Number], Value[Number]);
				Number++;
			}
		}
		else
		{
			PrimaryExp();
		}
	}
	else
	{
		UnaryOp();
		UnaryExp();
	}
	ofstream Output("output.txt", ios::app);
	Output << "<UnaryExp>" << endl;
	Output.close();
}
void MulExp()
{
	UnaryExp();
	ofstream Output("output.txt", ios::app);
	Output << "<MulExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "MULT" || Value[Number] == "DIV" || Value[Number] == "MOD")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			UnaryExp();
			ofstream Output("output.txt", ios::app);
			Output << "<MulExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
	
}
void AddExp() {
	MulExp();
	ofstream Output("output.txt", ios::app);
	Output << "<AddExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "PLUS" || Value[Number] == "MINU")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			MulExp();
			ofstream Output("output.txt", ios::app);
			Output << "<AddExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
}
void RelExp()
{
	AddExp();
	ofstream Output("output.txt", ios::app);
	Output << "<RelExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "GRE" || Value[Number] == "LSS" || Value[Number] == "GEQ" || Value[Number] == "LEQ")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			AddExp();
			ofstream Output("output.txt", ios::app);
			Output << "<RelExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
}
void EqExp()
{
	RelExp();
	ofstream Output("output.txt", ios::app);
	Output << "<EqExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "EQL" || Value[Number] == "NEQ")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			RelExp();
			ofstream Output("output.txt", ios::app);
			Output << "<EqExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
}
void LAndExp()
{
	EqExp();
	ofstream Output("output.txt", ios::app);
	Output << "<LAndExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "AND")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			EqExp();
			ofstream Output("output.txt", ios::app);
			Output << "<LAndExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
	
}
void LOrExp()
{
	LAndExp();
	ofstream Output("output.txt", ios::app);
	Output << "<LOrExp>" << endl;
	Output.close();
	while (true)
	{
		if (Value[Number] == "OR")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			LAndExp();
			ofstream Output("output.txt", ios::app);
			Output << "<LOrExp>" << endl;
			Output.close();
		}
		else
		{
			break;
		}
	}
}
void Cond()
{
	LOrExp();
	ofstream Output("output.txt", ios::app);
	Output << "<Cond>" << endl;
	Output.close();
}
void ConstExp()
{
	AddExp();
	ofstream Output("output.txt", ios::app);
	Output << "<ConstExp>" << endl;
	Output.close();
}
void Exp()
{
	AddExp();
	ofstream Output("output.txt", ios::app);
	Output << "<Exp>" << endl;
	Output.close();
}
void ConstInitVal()
{
	if (Value[Number] == "LBRACE")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		while (true)
		{
			if (Value[Number] == "RBRACE")
			{
				Write(Key[Number], Value[Number]);
				Number++;
				break;
			}
			else if (Value[Number] == "COMMA")
			{
				Write(Key[Number], Value[Number]);
				Number++;
				ConstInitVal();
			}
			else
			{
				ConstInitVal();
			}
		}
	}
	else
	{
		ConstExp();
	}
	ofstream Output("output.txt", ios::app);
	Output << "<ConstInitVal>" << endl;
	Output.close();
}
void ConstDef()
{
	Write(Key[Number], Value[Number]);
	Number++;
	while (true)
	{
		if (Value[Number] == "ASSIGN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			ConstInitVal();
			break;
		}
		else if (Value[Number] == "COMMA")
		{
			break;
		}
		else
		{
			Write(Key[Number], Value[Number]);
			Number++;
			int n = 1;
			while (n)
			{
				if (Value[Number] == "RBRACK")
				{
					n--;
					Write(Key[Number], Value[Number]);
					Number++;

				}
				else if (Value[Number] == "LBRACK")
				{
					n++;
					Write(Key[Number], Value[Number]);
					Number++;
				}
				else
				{
					ConstExp();
				}
			}
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<ConstDef>" << endl;
	Output.close();
}
void ConstDecl()
{
	Write(Key[Number], Value[Number]);
	Number++;
	BType();
	ConstDef();
	while (true)
	{
		if (Value[Number] == "SEMICN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			break;
		}
		else
		{
			Write(Key[Number], Value[Number]);
			Number++;
			ConstDef();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<ConstDecl>" << endl;
	Output.close();
}
void InitVal()
{
	if (Value[Number] == "LBRACE")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		while (true)
		{
			if (Value[Number] == "RBRACE")
			{
				Write(Key[Number], Value[Number]);
				Number++;
				break;
			}
			else if (Value[Number] == "COMMA")
			{
				Write(Key[Number], Value[Number]);
				Number++;
				InitVal();
			}
			else
			{
				InitVal();
			}
		}
	}
	else
	{
		Exp();
	}
	ofstream Output("output.txt", ios::app);
	Output << "<InitVal>" << endl;
	Output.close();
}
void VarDef()
{
	Write(Key[Number], Value[Number]);
	Number++;
	while (true)
	{
		if (Value[Number] == "ASSIGN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			InitVal();
			break;
		}
		else if (Value[Number] == "SEMICN")
		{
			break;
		}
		else if (Value[Number] == "COMMA")
		{
			break;
		}
		else
		{
			Write(Key[Number], Value[Number]);
			Number++;
			int n = 1;
			while (n)
			{
				if (Value[Number] == "RBRACK")
				{
					n--;
					Write(Key[Number], Value[Number]);
					Number++;

				}
				else if (Value[Number] == "LBRACK")
				{
					n++;
					Write(Key[Number], Value[Number]);
					Number++;
				}
				else
				{
					ConstExp();
				}
			}
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<VarDef>" << endl;
	Output.close();
}
void VarDecl()
{
	BType();
	VarDef();
	while (true)
	{
		if (Value[Number] == "SEMICN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			break;
		}
		else
		{
			Write(Key[Number], Value[Number]);
			Number++;
			VarDef();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<VarDecl>" << endl;
	Output.close();
}
void Decl()
{
	if (Value[Number] == "CONSTTK")
	{
		ConstDecl();
	}
	else
	{
		VarDecl();
	}
}
void FuncType()
{
	Write(Key[Number], Value[Number]);
	Number++;
	ofstream Output("output.txt", ios::app);
	Output << "<FuncType>" << endl;
	Output.close();
}
void FuncFParam()
{
	BType();
	Write(Key[Number], Value[Number]);
	Number++;
	while (true)
	{
		if (Value[Number] == "COMMA")
		{
			break;
		}
		else if (Value[Number] == "RPARENT")
		{
			break;
		}
		else if (Value[Number] == "LBRACK" || Value[Number] == "RBRACK")
		{
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else
		{
			ConstExp();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<FuncFParam>" << endl;
	Output.close();
}
void FuncFParams()
{
	while (true)
	{
		if (Value[Number] == "RPARENT")
		{
			break;
		}
		else if (Value[Number] == "COMMA")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			FuncFParam();
		}
		else
		{
			FuncFParam();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<FuncFParams>" << endl;
	Output.close();
}
void Stmt()
{
	if (Value[Number] == "IDENFR")
	{
		if (Value[Number + 1] == "LPARENT")
		{
			Exp();
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else
		{
			bool flag = false;
			int i = Number + 1;
			while (true)
			{
				if (Value[i] == "SEMICN")
				{
					break;
				}
				else if (Value[i] == "ASSIGN")
				{
					flag = true;
					break;
				}
				else
				{
					i++;
				}
			}
			if (flag)
			{
				LVal();
				Write(Key[Number], Value[Number]);
				Number++;
				if (Value[Number] == "GETINTTK")
				{
					Write(Key[Number], Value[Number]);
					Number++;
					Write(Key[Number], Value[Number]);
					Number++;
					Write(Key[Number], Value[Number]);
					Number++;
					Write(Key[Number], Value[Number]);
					Number++;
				}
				else
				{
					Exp();
					Write(Key[Number], Value[Number]);
					Number++;
				}
			}
			else
			{
				Exp();
				Write(Key[Number], Value[Number]);
				Number++;
			}
		}
	}
	else if (Value[Number] == "LBRACE")
	{
		Block();
	}
	else if (Value[Number] == "IFTK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;
		Cond();
		Write(Key[Number], Value[Number]);
		Number++;
		Stmt();
		if (Value[Number] == "ELSETK")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			Stmt();
		}
		else
		{

		}
	}
	else if (Value[Number] == "WHILETK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;
		Cond();
		Write(Key[Number], Value[Number]);
		Number++;
		Stmt();
	}
	else if (Value[Number] == "BREAKTK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;

	}
	else if (Value[Number] == "CONTINUETK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;
	}
	else if (Value[Number] == "RETURNTK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		if (Value[Number] == "SEMICN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else
		{
			Exp();
			Write(Key[Number], Value[Number]);
			Number++;
		}
	}
	else if (Value[Number] == "PRINTFTK")
	{
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;
		Write(Key[Number], Value[Number]);
		Number++;
		while (true)
		{
			if (Value[Number] == "RPARENT")
			{
				Write(Key[Number], Value[Number]);
				Number++;
				break;
			}
			else
			{
				Write(Key[Number], Value[Number]);
				Number++;
				Exp();
			}
		}
		Write(Key[Number], Value[Number]);
		Number++;
	}
	else
	{
		if (Value[Number] == "SEMICN")
		{
			Write(Key[Number], Value[Number]);
			Number++;
		}
		else
		{
			Exp();
			Write(Key[Number], Value[Number]);
			Number++;
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<Stmt>" << endl;
	Output.close();
}
void BlockItem()
{
	if (Value[Number] == "INTTK"||Value[Number]=="CONSTTK")
	{
		Decl();
	}
	else
	{
		Stmt();
	}
}
void Block()
{
	Write(Key[Number], Value[Number]);
	Number++;
	while (true)
	{
		if (Value[Number] == "RBRACE")
		{
			Write(Key[Number], Value[Number]);
			Number++;
			break;
		}
		else
		{
			BlockItem();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<Block>" << endl;
	Output.close();
}
void FuncDef()
{
	FuncType();
	Write(Key[Number], Value[Number]);
	Number++;
	Write(Key[Number], Value[Number]);
	Number++;
	if (Value[Number] == "RPARENT")
	{
		Write(Key[Number], Value[Number]);
		Number++;
	}
	else
	{
		FuncFParams();
		Write(Key[Number], Value[Number]);
		Number++;
	}
	Block();
	ofstream Output("output.txt", ios::app);
	Output << "<FuncDef>" << endl;
	Output.close();
}
void MainFuncDef()
{
	Write(Key[Number], Value[Number]);
	Number++;
	Write(Key[Number], Value[Number]);
	Number++;
	Write(Key[Number], Value[Number]);
	Number++;
	Write(Key[Number], Value[Number]);
	Number++;
	Block();
	ofstream Output("output.txt", ios::app);
	Output << "<MainFuncDef>" << endl;
	Output.close();
}
void CompUnit()
{
	while (true)
	{
		if (Value[Number] == "CONSTTK")
		{
			ConstDecl();
		}
		else if (Value[Number] == "INTTK")
		{
			if (Value[Number + 1] == "MAINTK")
			{
				MainFuncDef();
				break;
			}
			else
			{
				if (Value[Number + 2] == "LPARENT")
				{
					FuncDef();
				}
				else
				{
					VarDecl();
				}
			}
		}
		else
		{
			FuncDef();
		}
	}
	ofstream Output("output.txt", ios::app);
	Output << "<CompUnit>" << endl;
	Output.close();
}
void analysisGrammar()
{
	CompUnit();
}