#pragma once
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <sstream>
#include <vector>
using namespace std;
void init();
void write(std::string str1, std::string str2);
void note();
void keywordOrIdentifier();
void str();
void number();
void oper();
void analysis();
void readtext();
void copy(vector<string> &k, vector<string> &v);