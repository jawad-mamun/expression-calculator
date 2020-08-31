package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, values for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		
		//gets rid of any potential spaces in the expression. 
		expr = expr.replaceAll("\\s+", "");
		/*
		 * specifies delimiters for separating tokens (the delimters are not tokens themselves - these 
		 * were chosen to make the tokens consist of 
		 * only variable names and the left bracket ( [ ) of arrays in the expression
		 */
		String vdelims = " \t*/+-/1234567890()]";
		StringTokenizer st = new StringTokenizer(expr, vdelims);
		
		//iterates through the tokens. 
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			//checks if there is a bracket in the token- indicating an array is present in the token
			if (s.indexOf('[') != -1) {
				//checks to see if the bracket is at the end of the token- meaning the entire token is just the array
				if (s.indexOf('[') == s.length() - 1) {
					boolean isNewArray = true;
					//iterates through the arrays ArrayList to check if the array has been registered
					for (int i = 0; i < arrays.size(); i++) {
						//changes boolean isNewArray to false if the array is already present in the ArrayList
						if (arrays.get(i).name.equals(s.substring(0, s.length() - 1))) {
							isNewArray = false;
						}
					}
					//adds the array to the arrays ArrayList if it is not already there
					if (isNewArray) {
						arrays.add(new Array(s.substring(0, s.indexOf('['))));
					}
				}
				//checks to see if the bracket is elsewhere- meaning the token consists of an array and a variable
				else {
					//isolates and recursively passes in just the variable part of the token
					makeVariableLists(s.substring(s.indexOf('[') + 1, s.length()), vars, arrays);
					//isolates the part of the token with just the array
					String t = s.substring(0, s.indexOf('['));
					boolean isNewArray = true;
					//iterates through the arrays ArrayList to check if the array has been registered
					for (int i = 0; i < arrays.size(); i++) {
						//changes boolean isNewArray to false if the array is already present in the ArrayList
						if (arrays.get(i).name.equals(t)) {
							isNewArray = false;
						}
					}
					//adds the array to the arrays ArrayList if it is not already there
					if (isNewArray) {
						arrays.add(new Array(t));
					}
				}
			} 
			//checks if there is no bracket in the token- this means the token is just a variable
			else if (s.indexOf('[') == -1) {
				boolean isNewVariable = true;
				//traverses through list of variables to check if the current variable has already been
				//entered
				for (int i = 0; i < vars.size(); i++) {
					//changes boolean isNewVariable to false if the variable already was registered
					if (vars.get(i).name.equals(s)) {
						isNewVariable = false;
					}
				}
				//adds variable to vars ArrayList if the variable is not already there
				if (isNewVariable) {
					vars.add(new Variable(s));
				}
			}
		}
		//prints out the arraylists of variables and arrays in the expression for user to see
		System.out.println(vars);
		System.out.println(arrays);
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars   The variables array list, with values for all variables in the
	 *               expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		//final answer of the expression that will be returned
		float eval = 0;
		//temp used to push the result of subexpressions into the operands stack
		float temp = 0;
		//gets rid of any spaces in the expression as these aren't important for calculation
		expr = expr.replaceAll("\\s+", "");
		//handles case of empty expression
		if(expr.equals("")) return (float) 0.0;
		String prev = "";
		//checks if there are no parenthesis or brackets in the expression- meaning there are no arrays or variables used
		if (expr.indexOf('(') == -1 && expr.indexOf('[') == -1) {
			Stack<Float> operands = new Stack<Float> ();
			Stack<String> operators = new Stack<String> ();
			//these delimiters are included as tokens themselves
			StringTokenizer st = new StringTokenizer(expr, " \t*/+-/()]", true);
			//iterates through the tokens
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				//checks to see if operater is multiplication
				if (token.equals("*")){
					//gets the term after the multiplication sign
					String termAfterMult = st.nextToken();
					token = termAfterMult;
					//checks to see if the termAfterMult is not a negative number
					if (termAfterMult.charAt(0)!= '-') {
						//if the termAfterMult is a number, pops the operands stack and multiplies
						//appropriate quantities together
						if (Character.isDigit(termAfterMult.charAt(0))){
							temp = operands.pop() * Float.valueOf(termAfterMult);
						}
						//this is if the termAfterMult is a variable
						else {
							int a = 0;
							//goes through the variables array to get the value of the variable
							for (int i = 0; i < vars.size(); i++) {
								Variable x = vars.get(i);
								if (x.name.equals(termAfterMult)) {
									a = x.value;
									break;
								}
							}
							//pops the operands stack and multiplies the variable with the appropriate quantity
							temp = operands.pop() * a;
						} 
					}
					//this is if the termAfterMult is a negative number
					else {
						//gets the absolute value of the negative term after the termAfterMult '-' character
						String negative = st.nextToken();
						token = negative;
						//checks if the negative term is a variable
						if (Character.isLetter(negative.charAt(0))){
							//goes through the variables array to get the value of the variable
							//pops operands stack and multiplies negative variable with appropriate quantity
							for (int i = 0; i < vars.size(); i++) {
								Variable x = vars.get(i);
								if (x.name.equals(negative)) {
									temp = operands.pop() * ((float)x.value*-1);
									break;
								}
							}
						}
						//checks if negative term is just a number, multiplies accordingly
						else {
							temp = operands.pop() * (Float.valueOf(negative)*-1);
						}
					}
					//pushes the result of the multiplication back into the stack
					operands.push(temp);
				}
				//same process as multiplication code above, but with division
				else if (token.equals("/")){
					
					String termAfterDiv = st.nextToken();
					token = termAfterDiv;
					if (termAfterDiv.charAt(0)!= '-') {
					if (Character.isDigit(termAfterDiv.charAt(0))){
						temp = operands.pop() / Float.valueOf(termAfterDiv);
					}
					else {
						int a = 0;
						for (int i = 0; i < vars.size(); i++) {
							Variable x = vars.get(i);
							if (x.name.equals(termAfterDiv)) {
								a = x.value;
								break;
							}
						}
						temp = operands.pop() / a;
					} }
					
					else {
						String negative = st.nextToken();
						token = negative;
						if (Character.isLetter(negative.charAt(0))){
							for (int i = 0; i < vars.size(); i++) {
								Variable x = vars.get(i);
								if (x.name.equals(negative)) {
									temp = operands.pop() / ((float)x.value*-1);
									break;
								}
							}
						}
						else {
							temp = operands.pop() / (Float.valueOf(negative)*-1);
						}
					}
					
					operands.push(temp);
				}
				//checks if the operator is subtraction or addition
				else if (token.equals("-") || token.equals("+")) {
					//pushes the operator to the operators stack if the previous token was a variable or number
					//this is mainly to check for negative numbers, as there may be an operator before the "-"
					if (!prev.equals("+") && !prev.equals("-") && !prev.equals("*") && !prev.equals("/") && !prev.equals("") && !prev.equals("(") && !prev.equals("[")) {
					operators.push(token);
					}
					//if the "operator" is actually a negative sign and not really an operator
					else {
						//pushes the value of the negative operand to the operands stack
						String negative = st.nextToken();
						token = negative;
						if (Character.isLetter(negative.charAt(0))){
							for (int i = 0; i < vars.size(); i++) {
								Variable x = vars.get(i);
								if (x.name.equals(negative)) {
									operands.push((float)x.value*-1);
								}
							}
						}
						else {
							operands.push(Float.valueOf(negative)*-1);
						}
					}
				}
				//checks if the token is a variable, finds the value of the variable, and pushes the value to the operands stack
				else if (Character.isLetter(token.charAt(0))){
					for (int i = 0; i < vars.size(); i++) {
						Variable x = vars.get(i);
						if (x.name.equals(token)) {
							operands.push((float)x.value);
							break;
						}
					}
				}
				else if (token.equals(")") || token.equals("]")) {
					//do nothing
				}
				//if the token is a positive number, pushes the number to the operands stack
				else {
					operands.push(Float.valueOf(token));
				}
			//keeps track of previous token
			prev = token;
			}
			
			//stacks to hold the operands and operators in the right order
			Stack<Float> temp1 = new Stack<Float>();
			Stack<String> temp2 = new Stack<String>();
			
			//because the operators and operands are now in reverse order in the stack, their order is reversed back to 
			//the original order in the temp1 and temp2 stacks
			while (!operands.isEmpty()) {
				temp1.push(operands.pop());
			}
			
			while (!operators.isEmpty()) {
				temp2.push(operators.pop());
			}
			
			//correctly reassigns operands and operators to their appropriate orders
			operands=temp1;
			operators=temp2;
			
			//multiplication and division were handled before, this is only for addition and subtraction- this is to ensure
			//the correct order of operations
			while (!operators.isEmpty()) {
				//if subtraction, pops the operator and 2 operands, changes the value of one of the operands to negative
				//then pushes the two operands back in as well as a "+" operator
				//this allows for the subtraction to be handled as addition
				if (operators.peek().equals("-")) {
					operators.pop();
					float a = operands.pop();
					float pushback = operands.pop()*-1;
					operands.push(pushback);
					operands.push(a);
					operators.push("+");
				}
				//if addition, pops the operator and 2 operands, adds the two operands together, and pushes the result back to operands
				else if (operators.peek().equals("+")) {
					operators.pop();
					temp = operands.pop() + operands.pop();
					operands.push(temp);
				}
			}
			//pops the final answer and returns it
			eval = operands.pop();
			return eval;
		}
		
		//if the expression contains brackets (which indicates arrays) or parenthesis
		int paren = expr.indexOf('(');
		int brack = expr.indexOf('[');
		
		//if the expression has no brackets or the parenthesis comes before the bracket
		if (brack==-1 || (paren!=-1 && paren<brack)) {
			
			Stack<Integer> paren1 = new Stack<Integer>();
			//keeps doing this until there are no more parenthesis in the expression
			while (expr.indexOf('(')!=-1) {
			//iterates through characters in the expression
			for (int i = 0; i<expr.length(); i++) {
				//pushes position of opening parenthesis into the paren1 stack
				if (expr.charAt(i)=='(') {
					paren1.push(i);
				}
				//once the closing parenthesis is reached, recursively calls the evaluate method for the subexpression
				//in between the two parenthesis
				//then, replaces the subexpression in of the parenthesis with its appropriate value
				else if (expr.charAt(i)==')') {
					int a = paren1.pop();
					float recur = evaluate (expr.substring(a+1, i), vars, arrays);
					expr = expr.replace(expr.substring(a, i+1), Float.toString(recur));
				}
			}
			}
			//recursively calls the evaluate method for the expression, which now has no parenthesis
			return evaluate (expr, vars, arrays);
		}
		//if the expression has no parenthesis or the brackets come before the parenthesis
		if (paren==-1 || (brack!=-1 && brack<paren)) {
			Stack<Integer> brack2 = new Stack<Integer>();
			//traverses through expr using a while loop
			int r = 0;
			while (r<expr.length()) {
				//pushes the index of opening bracket to brack2
				if(expr.charAt(r) == '[') {
					brack2.push(r);
					r++;
				}
				//if a closing bracket is reached, pops the opening bracket and recursively sends the subexpression
				//in between the brackets to the evaluate method
				//replaces the subexpression in between the brackets with the corresponding value
				//sets r back to the next index in the original expr
				else if (expr.charAt(r) == ']') {
					int a = brack2.pop();
					float recur = evaluate (expr.substring(a+1, r), vars, arrays);
					expr = expr.replace(expr.substring(a+1, r), Float.toString(recur));
					r= expr.indexOf(']', a)+1;
				}
				else {
					r++;
				}
			}
			StringTokenizer st = new StringTokenizer(expr, " \t*/+-/", true);
			Array x = null;
			String arr = "";
			while (st.hasMoreTokens()) {
				String curr = st.nextToken();
				//checks if there is an opening bracket in the token
				if(curr.indexOf('[')!=-1) {
					Stack<Integer> brack1 = new Stack<Integer>();
					//keeps going until there is no more opening bracket in the token
					while (curr.indexOf('[')!=-1) {
						//iterates through the token
						for (int i = 0; i<curr.length(); i++) {
							//pushes the index of the opening bracket to brack1
							if (curr.charAt(i)=='[') {
								brack1.push(i);
							}
							//if there is a closing bracket, pops the index of the opening bracket and recursively calls evaluate 
							//for the subexpression inside the brackets
							else if (curr.charAt(i) == ']') {
								int a = brack1.pop();
								float recur = evaluate (curr.substring(a+1, i), vars, arrays);
								//gets the array name (the brackets indicate an array)
								String arrayname = curr.substring(0,a);
								//finds the array that the brackets are referring to from arrays ArrayList
								for (int z = 0; z < arrays.size(); z++) {
									x = arrays.get(z);
									if (x.name.equals(arrayname)) {
										break;
									}
								}
								//gets value that the brackets in the expr are referring to from the right array
								int u = x.values[(int)recur];
								//replaces the entire array bracket with the right value
								curr = curr.replace(curr, Integer.toString(u));
							}
						}
					}
				}
			//concatenates the value of the brackets with the rest of the expression
			arr = arr+curr;	
			}
			//recursively calls the evaluate function, with all the brackets accounted for
			return evaluate (arr, vars, arrays);
		}
		
		return 0;
	}
}
