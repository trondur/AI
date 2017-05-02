import java.util.ArrayList;


public class SudokuSolver implements ISudokuSolver {

	int[][] puzzle;
	int size;
	ArrayList<ArrayList<Integer>> D;
	
	public int[][] getPuzzle() {
		return puzzle;
	}

	public void setValue(int col, int row, int value) {
		puzzle[col][row] = value;
	}

	public void setup(int size1) {
		size = size1;

		puzzle = new int[size*size][size*size];

		D = new ArrayList<ArrayList<Integer>>(size*size*size*size);
		
		for (int i = 0; i < size*size*size*size; i++) {
			ArrayList<Integer> square = new ArrayList<Integer>(size*size);
			for (int j = 1; j <= (size*size); j++) {
				square.add(j);
			}
			D.add(square);
		}	
	}

	public boolean solve() {

		ArrayList<Integer> asn = GetAssignment(puzzle);
		boolean solved = false;
		if (INITIAL_FC(asn)) {
			asn = FC(asn);
			if (asn != null) {
				puzzle = GetPuzzle(asn);
				solved = true;
			}
		}
		return solved;
	}

	public void readInPuzzle(int[][] p) {
		puzzle = p;
	}
	
	public ArrayList<Integer> FC(ArrayList<Integer> asn) {

		int unassignedIdx = asn.indexOf(0);
		if(unassignedIdx == -1) return asn; // return if there is no unassigned variable

		ArrayList<ArrayList<Integer>> backupD = cloneNestedArray(D);

		ArrayList<Integer> assignmentBackup = cloneArray(D.get(unassignedIdx));
		for (int assignment : assignmentBackup) {
			if(AC_FC(unassignedIdx, assignment)){
				asn.set(unassignedIdx, assignment);

				ArrayList<Integer> newAssignment = FC(asn);
				if(newAssignment != null){
					return newAssignment;
				}
				asn.set(unassignedIdx, 0);
				D = cloneNestedArray(backupD);
			}
			else{
				D = cloneNestedArray(backupD);
			}
		}
		return null;//failure
	}
	
	// Implementation of acr-consistency for forward-checking AC-FC(cv).
	public boolean AC_FC(Integer X, Integer V){
		//Reduce domain Dx
		D.get(X).clear();
		D.get(X).add(V);
		
		//Put in Q all relevant Y where Y>X
		ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;
		
		//all variables in the same column
		for (int i=0; i<size*size; i++){
			if (GetVariable(i,col) > X) {
				Q.add(GetVariable(i,col));
			}
		}
		//all variables in the same row
		for (int j=0; j<size*size; j++){
			if (GetVariable(row,j) > X) {
				Q.add(GetVariable(row,j));
			}
		}
		//all variables in the same size*size box
		for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
			for (int j=cell_y*size; j<=cell_y*size + 2; j++){
				if (GetVariable(i,j) > X) {
					Q.add(GetVariable(i,j));
				}
			}
		}
	
		//REVISE(Y,X)
		boolean consistent = true;
		while (!Q.isEmpty() && consistent){
			Integer Y = Q.remove(0);
			if (REVISE(Y,X)) {
				consistent = !D.get(Y).isEmpty();
			}
		}
		return consistent;
	}	
		
	public boolean REVISE(int Xi, int Xj){
		Integer zero = new Integer(0);
		
		assert(Xi >= 0 && Xj >=0);
		assert(Xi < size*size*size*size && Xj <size*size*size*size);
		assert(Xi != Xj);
		
		boolean DELETED = false;

		ArrayList<Integer> Di = D.get(Xi);
		ArrayList<Integer> Dj = D.get(Xj);	
		
		for (int i=0; i<Di.size(); i++){
			Integer vi = Di.get(i);
			ArrayList<Integer> xiEqVal = new ArrayList<Integer>(size*size*size*size);	
			for (int var=0; var<size*size*size*size; var++){
				xiEqVal.add(var,zero);				
			}

			xiEqVal.set(Xi,vi);
			
			boolean hasSupport = false;	
			for (int j=0; j<Dj.size(); j++){
				Integer vj = Dj.get(j);
				if (CONSISTENT(xiEqVal, Xj, vj)) {
					hasSupport = true;
					break;
				}
			}			
			if (hasSupport == false) {
				Di.remove(vi);
				DELETED = true;
			}
		}		
		return DELETED;
	}
				
	public boolean CONSISTENT(ArrayList<Integer> asn, Integer variable, Integer val) {
		Integer v1,v2;
		
		//variable to be assigned must be clear
		assert(asn.get(variable) == 0);
		asn.set(variable,val);

		//alldiff(col[i])
	 	for (int i=0; i<size*size; i++) {
	 		for (int j=0; j<size*size; j++) {
	 			for (int k=0; k<size*size; k++) {
		 			if (k != j) {
		 				v1 = asn.get(GetVariable(i,j));
		 				v2 = asn.get(GetVariable(i,k));
			 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
			 				asn.set(variable,0);
			 				return false;
			 			}
			 		}
	 			}
	 		}
	 	}
	
	 	//alldiff(row[j])
	 	for (int j=0; j<size*size; j++) {
	 		for (int i=0; i<size*size; i++) {
	 			for (int k=0; k<size*size; k++) {
		 			if (k != i) {
		 				v1 = asn.get(GetVariable(i,j));
		 				v2 = asn.get(GetVariable(k,j));
			 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
			 				asn.set(variable,0);			 				
			 				return false;
			 			}
		 			}
	 			}
	 		}
	 	}
	 	
	 	//alldiff(block[size*i,size*j])
	 	for (int i=0; i<size; i++) {
	 		for (int j=0; j<size; j++) {
	 			for (int i1 = 0; i1<size; i1++) {
	 				for (int j1=0; j1<size; j1++) {
	 					int var1 = GetVariable(size*i + i1, size*j + j1);
	 		 			for (int i2 = 0; i2<size; i2++) {
	 		 				for (int j2=0; j2<size; j2++) {
	 		 					int var2 = GetVariable(size*i+i2, size*j + j2);
	 		 					if (var1 != var2) {
	 				 				v1 = asn.get(var1);
	 				 				v2 = asn.get(var2);
	 		 			 			if (v1 != 0 && v2 != 0 && v1.compareTo(v2) == 0) {
	 					 				asn.set(variable,0);	 		 			 				
	 					 				return false;
	 					 			}
	 		 					}
	 		 				}
	 		 			}
 
	 				}
	 			}
	 		}
	 	}
		asn.set(variable,0);
		return true;
	}	
		
	public boolean INITIAL_FC(ArrayList<Integer> anAssignment) {
		//Enforces consistency between unassigned variables and all 
		//initially assigned values; 
		for (int i=0; i<anAssignment.size(); i++){
			Integer V = anAssignment.get(i);
			if (V != 0){
				ArrayList<Integer> Q = GetRelevantVariables(i);
				boolean consistent = true;
				while (!Q.isEmpty() && consistent){
					Integer Y =  Q.remove(0);
					if (REVISE(Y,i)) {
						consistent = !D.get(Y).isEmpty();
					}
				}	
				if (!consistent) return false;
			}
		}
		return true;
	}
		
	public ArrayList<Integer> GetRelevantVariables(Integer X){
		//Returns all variables that are interdependent of X, i.e. 
		//all variables involved in a binary constraint with X
		ArrayList<Integer> Q = new ArrayList<Integer>(); //list of all relevant Y
		int col = GetColumn(X);
		int row = GetRow(X);
		int cell_x = row / size;
		int cell_y = col / size;
		
		//all variables in the same column
		for (int i=0; i<size*size; i++){
			if (GetVariable(i,col) != X) {
				Q.add(GetVariable(i,col));
			}
		}
		//all variables in the same row
		for (int j=0; j<size*size; j++){
			if (GetVariable(row,j) != X) {
				Q.add(GetVariable(row,j));
			}
		}
		//all variables in the same size*size cell
		for (int i=cell_x*size; i<=cell_x*size + 2; i++) {
			for (int j=cell_y*size; j<=cell_y*size + 2; j++){
				if (GetVariable(i,j) != X) {
					Q.add(GetVariable(i,j));
				}
			}
		}			
		return Q;
	}

	//------------------------------------------------------------------
	// Functions translating between the puzzle and an assignment
	//-------------------------------------------------------------------
	public ArrayList<Integer> GetAssignment(int[][] p) {
		ArrayList<Integer> asn = new ArrayList<Integer>();
		for (int i=0; i<size*size; i++) {
			for (int j=0; j<size*size; j++) {
				asn.add(GetVariable(i,j), new Integer(p[i][j]));
				if (p[i][j] != 0){
						//restrict domain
						D.get(GetVariable(i,j)).clear();
						D.get(GetVariable(i,j)).add(new Integer(p[i][j]));
					}
			}
		}
		return asn;
	}	

	public int[][] GetPuzzle(ArrayList<Integer> asn) {
		int[][] p = new int[size*size][size*size];
		for (int i=0; i<size*size; i++) {
			for (int j=0; j<size*size; j++) {
				Integer val = asn.get(GetVariable(i,j));
				p[i][j] = val.intValue();
			}
		}
		return p;
	}

	//------------------------------------------------------------------
	//Utility functions
	//-------------------------------------------------------------------
	public int GetVariable(int i, int j){
		assert(i<size*size && j<size*size);
		assert(i>=0 && j>=0);		
		return (i*size*size + j);	
	}	
	
	public int GetRow(int X){
		return (X / (size*size)); 	
	}	
	
	public int GetColumn(int X){
		return X - ((X / (size*size))*size*size);	
	}	

	private ArrayList<ArrayList<Integer>> cloneNestedArray(ArrayList<ArrayList<Integer>> arr) {
		ArrayList<ArrayList<Integer>> clone = new ArrayList<ArrayList<Integer>>();
		for (ArrayList<Integer> nestedArray : arr) {
			clone.add(cloneArray(nestedArray));
		}
		return clone;
	}

	private ArrayList<Integer> cloneArray(ArrayList<Integer> arr){
		return new ArrayList<Integer>(arr);
	}
}
