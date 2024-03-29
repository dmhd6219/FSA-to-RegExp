// Sviatoslav Sviatkin CS-05

import java.io.*;
import java.util.*;

/**
 * The Main class.
 */
public class Main {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader("fsa.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("result.txt"));
        ArrayList<String> s = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            s.add(bf.readLine());
        }

        try {
            FSA fsa = new FSA(s);
            bw.write(fsa.toRegExp());
            bw.write("\n");

        } catch (FSAException e) {
            bw.write("Error:\n");
            bw.write(e.getMessage());
            bw.write("\n");
        }

        bw.close();

    }
}

/**
 * Class that represents Finite State Machine.
 */
class FSA {
    /**
     * Array of states.
     */
    ArrayList<String> states = new ArrayList<>();
    /**
     * Array with alphabet.
     */
    ArrayList<String> alphabet = new ArrayList<>();
    /**
     * Array with Initial states.
     */
    ArrayList<String> initialStates = new ArrayList<>();
    /**
     * Array with Final states.
     */
    ArrayList<String> finalStates = new ArrayList<>();
    /**
     * Array with Transitions.
     */
    ArrayList<String[]> transitions = new ArrayList<>();

    /**
     * The Undirected graph.
     * key : state, value : child states
     */
    HashMap<String, HashSet<String>> undirectedGraph = new HashMap<>();
    /**
     * The Directed graph.
     * key : state, value : {key : transition, value : child states}
     */
    HashMap<String, HashMap<String, HashSet<String>>> directedGraph = new HashMap<>();


    /**
     * Instantiates a new FSA.
     *
     * @param data input strings
     * @throws FSAException the FSA Exception
     */
    FSA(ArrayList<String> data) throws FSAException {
        processData(data);

        isDisjoint();
        isDeterministic();

        toRegExp();
    }

    /**
     * Handles all input strings.
     *
     * @param data input strings
     * @throws FSAException the FSA Exception
     */
    private void processData(ArrayList<String> data) throws FSAException {
        processStates(data.get(0));
        processAlphas(data.get(1));
        processInitState(data.get(2));
        processFinalState(data.get(3));
        processTransitions(data.get(4));

        for (String state : this.states) {
            this.undirectedGraph.put(state, new HashSet<>());
            this.directedGraph.put(state, new HashMap<>());
            for (String a : this.alphabet) {
                this.directedGraph.get(state).put(a, new HashSet<>());
            }
        }

        for (String[] transition : this.transitions) {
            this.undirectedGraph.get(transition[0]).add(transition[2]);
            this.undirectedGraph.get(transition[2]).add(transition[0]);

            this.directedGraph.get(transition[0]).get(transition[1]).add(transition[2]);
        }


    }

    /**
     * Handles input string with {@code states}.
     *
     * @param s input strings
     * @throws FSAException the FSA Exception
     */
    private void processStates(String s) throws FSAException {
        if (!s.startsWith("states=[") || !s.endsWith("]")) {
            throw new FSAException(Errors.E0.getValue());
        }

        String[] states = s.substring(8, s.length() - 1).split(",");
        for (String state : states) {
            if (state.length() == 0) {
                continue;
            }

            if (!FSA.isGoodStateName(state)) {
                throw new FSAException(Errors.E0.getValue());
            }
            this.states.add(state);
        }
    }

    /**
     * Handles input string with {@code alphabet}.
     *
     * @param s input strings
     * @throws FSAException the FSA Exception
     */
    private void processAlphas(String s) throws FSAException {
        if (!s.startsWith("alpha=[") || !s.endsWith("]")) {
            throw new FSAException(Errors.E0.getValue());
        }

        String[] alphas = s.substring(7, s.length() - 1).split(",");
        for (String alpha : alphas) {
            if (alpha.length() == 0) {
                continue;
            }

            if (!FSA.isGoodAlphaName(alpha)) {
                throw new FSAException(Errors.E0.getValue());
            }
            this.alphabet.add(alpha);
        }
    }

    /**
     * Handles input string with {@code initialStates}.
     *
     * @param s input strings
     * @throws FSAException the FSA Exception
     */
    private void processInitState(String s) throws FSAException {
        if (!s.startsWith("initial=[") || !s.endsWith("]")) {
            throw new FSAException(Errors.E0.getValue());
        }

        String[] initStates = s.substring(9, s.length() - 1).split(",");

        for (String state : initStates) {
            if (state.length() == 0) {
                continue;
            }
            if (!this.states.contains(state)) {
                throw new FSAException(Errors.E1.getValue().formatted(state));
            }
            this.initialStates.add(state);
        }

        if (this.initialStates.size() < 1) {
            throw new FSAException(Errors.E4.getValue());
        }
        if (this.initialStates.size() > 1) {
            throw new FSAException(Errors.E0.getValue());
        }
    }

    /**
     * Handles input string with {@code finalStates}.
     *
     * @param s input strings
     * @throws FSAException the FSA Exception
     */
    private void processFinalState(String s) throws FSAException {
        if (!s.startsWith("accepting=[") || !s.endsWith("]")) {
            throw new FSAException(Errors.E0.getValue());
        }

        String[] finalStates = s.substring(11, s.length() - 1).split(",");
        for (String state : finalStates) {
            if (state.length() == 0) {
                continue;
            }
            if (!this.states.contains(state)) {
                throw new FSAException(Errors.E1.getValue().formatted(state));
            }
            this.finalStates.add(state);
        }
    }

    /**
     * Handles input string with {@code transitions}.
     *
     * @param s input strings
     * @throws FSAException the FSA Exception
     */
    private void processTransitions(String s) throws FSAException {
        if (!s.startsWith("trans=[") || !s.endsWith("]")) {
            throw new FSAException(Errors.E0.getValue());
        }

        String[] transitions = s.substring(7, s.length() - 1).split(",");
        for (String trans : transitions) {
            if (trans.length() == 0) {
                continue;
            }
            String[] transition = trans.split(">");

            if (!this.states.contains(transition[0])) {
                throw new FSAException(Errors.E1.getValue().formatted(transition[0]));
            }
            if (!this.alphabet.contains(transition[1])) {
                throw new FSAException(Errors.E3.getValue().formatted(transition[1]));
            }
            if (!this.states.contains(transition[2])) {
                throw new FSAException(Errors.E1.getValue().formatted(transition[2]));
            }

            if (transition.length != 3) {
                throw new FSAException(Errors.E0.getValue());
            }
            this.transitions.add(transition);
        }
    }

    /**
     * Checks if name for {@code State} is good.
     *
     * @param s name
     * @return {@code true} if name is good, otherwise {@code false}
     */
    public static boolean isGoodStateName(String s) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";

        for (char c : s.toCharArray()) {
            if (alphabet.indexOf(c) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if name for {@code alphabet} is good.
     *
     * @param s name
     * @return {@code true} if name is good, otherwise {@code false}
     */
    public static boolean isGoodAlphaName(String s) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789_";

        for (char c : s.toCharArray()) {
            if (alphabet.indexOf(c) < 0) {
                return false;
            }
        }

        return true;
    }


    /**
     * Checks if this {@link FSA} has some disjoint states.
     */
    private void isDisjoint() throws FSAException {
        ArrayList<String> visited = recursiveSearchInUndirectedGraph(this.initialStates.get(0), new ArrayList<>());
        if (visited.size() < this.states.size()) {
            throw new FSAException(Errors.E2.getValue());
        }
    }

    /**
     * Checks if this {@link FSA} is deterministic.
     */
    private void isDeterministic() throws FSAException {
        for (HashMap<String, HashSet<String>> transition : this.directedGraph.values()) {
            for (HashSet<String> states : transition.values()) {
                if (states.size() > 1) {
                    throw new FSAException(Errors.E5.getValue());
                }
            }
        }
    }


    /**
     * Checks which states are joint.
     *
     * @return List with visited states
     */
    private ArrayList<String> recursiveSearchInUndirectedGraph(String start, ArrayList<String> visited) {
        for (String state : this.undirectedGraph.get(start)) {
            if (!visited.contains(state)) {
                visited.add(state);
                recursiveSearchInUndirectedGraph(state, visited);
            }
        }
        return visited;
    }

    /**
     * Gets initial Regular Expressions.
     *
     * @return array with initial regular expressions
     */
    private String[][] getInitialRegExp() {
        String[][] initRegExp = new String[this.states.size()][this.states.size()];

        for (int i = 0; i < this.states.size(); i++) {
            String state = this.states.get(i);

            for (int j = 0; j < this.states.size(); j++) {
                String newState = this.states.get(j);
                StringBuilder regExp = new StringBuilder();

                for (String[] trans : this.transitions) {
                    if (trans[0].equals(state) && trans[2].equals(newState)) {
                        regExp.append(trans[1]).append("|");
                    }
                }

                if (state.equals(newState)) {
                    regExp.append("eps");
                }

                if (regExp.toString().equals("")) {
                    regExp = new StringBuilder("{}");
                }

                if (regExp.charAt(regExp.length() - 1) == '|') {
                    regExp = new StringBuilder(regExp.substring(0, regExp.length() - 1));
                }

                initRegExp[i][j] = regExp.toString();
            }
        }

        return initRegExp;
    }

    /**
     * Gets Indexes of Accepting States in List of all States.
     *
     * @return List of Indexes of Accepting States in List of all States
     */
    private ArrayList<Integer> getFinalStatesIndices() {
        ArrayList<Integer> ans = new ArrayList<>();
        for (int i = 0; i < this.states.size(); i++) {
            if (this.finalStates.contains(this.states.get(i))) {
                ans.add(i);
            }
        }
        return ans;
    }

    /**
     * Gets Regular Expression from this FSA.
     *
     * @return Regular Expression from this FSA
     */
    public String toRegExp() {
        String[][] regExp = this.getInitialRegExp();
        ArrayList<Integer> finalStatesIndices = this.getFinalStatesIndices();

        for (int k = 0; k < this.states.size(); k++) {
            String[][] newRegExp = new String[this.states.size()][this.states.size()];


            for (int i = 0; i < this.states.size(); i++) {
                for (int j = 0; j < this.states.size(); j++) {
                    newRegExp[i][j] = String.format("(%s)(%s)*(%s)|(%s)", regExp[i][k],
                            regExp[k][k], regExp[k][j], regExp[i][j]);

                }
            }

            regExp = newRegExp;
        }

        StringBuilder resultNewRegExp = new StringBuilder();
        for (int i : finalStatesIndices) {
            resultNewRegExp.append(regExp[0][i]).append("|");
        }

        String ans = "";
        if (resultNewRegExp.toString().equals("")) {
            ans = "{}";
        } else {
            ans = resultNewRegExp.substring(0, resultNewRegExp.length() - 1);
        }
        return ans;
    }
}

/**
 * The {@link Exception} for all FSA errors.
 */
class FSAException extends Exception {

    /**
     * Instantiates a new FSA exception.
     *
     * @param s the error message
     */
    FSAException(String s) {
        super(s);
    }

}

/**
 * Messages for FSA.
 */
enum Errors {
    E0("E0: Input file is malformed"),
    /**
     * The Error #1.
     */
    E1("E1: A state '%s' is not in the set of states"),
    /**
     * The Error #2.
     */
    E2("E2: Some states are disjoint"),
    /**
     * The Error #3.
     */
    E3("E3: A transition '%s' is not represented in the alphabet"),
    /**
     * The Error #4.
     */
    E4("E4: Initial state is not defined"),
    /**
     * The Error #5.
     */
    E5("E5: FSA is nondeterministic");

    private final String value;

    Errors(String value) {
        this.value = value;
    }

    /**
     * Returns message.
     *
     * @return the string
     */
    public String getValue() {
        return this.value;
    }
}