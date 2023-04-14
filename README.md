# FSA-to-RegExp

## Description

Given an FSA description in the `input.txt`, your program should output the `output.txt` containing an error description or a regular expression that corresponds to the given FSA. The regular expression should be built according to a slightly modified version of the **Kleene’s algorithm**.

## Input file format
```
states=[s1,s2,...]	  // s1 , s2, ... ∈ latin letters, words and numbers
alpha=[a1,a2, ...]	  // a1 , a2, ... ∈ latin letters, words, numbers and character '_’(underscore)
init.st=[s]	          // s ∈ states
fin.st=[s1,s2,...]	  // s1, s2 ∈ states
trans=[s1>a>s2,... ]  // s1,s2,...∈ states; a ∈ alpha
```

## Validation result
### Errors:
* E1: A state 's' is not in the set of states
* E2: Some states are disjoint
* E3: A transition 'a' is not represented in the alphabet
* E4: Initial state is not defined
* E5: Input file is malformed


## Kleene's Algorithm
It transforms a given deterministic finite state automaton (FSA) into a regular expression.

Given an FSA M = (Q, A, δ, q<sub>0</sub>, F), with Q = {q<sub>0</sub>, . . . , q<sub>n</sub>} its set of states, the algorithm computes:
* the sets R<sub>ij</sub><sup>k</sup> of all strings that take M from state q<sub>i</sub> to q<sub>j</sub> without going through any state numbered higher than k
* each set R<sub>ij</sub><sup>k</sup> is represented by a regular expression
* the algorithm computes them step by step for k = −1, 0, ... , n
* since there is no state numbered higher than n, the regular expression R<sub>0j</sub><sup>n</sup> represents the set of all strings that take M from its start state q<sub>0</sub> to q<sub>j</sub>
    * If F = {q<sub>1</sub>, ... , q<sub>f</sub>} is the set of accept states, the regular expression R<sub>01</sub><sup>n</sup>\| ... \|R<sub>0f</sub><sup>n</sup> represents the language accepted by M

The initial regular expression, for k = -1, are computed as:
* R<sub>ij</sub><sup>-1</sup> = a<sub>1</sub> \| ... \| a<sub>m</sub> if i ≠ j, where δ(q<sub>i</sub>, a<sub>1</sub>) = ... = δ(q<sub>i</sub>, a<sub>m</sub>) = q<sub>j</sub>
* R<sub>ij</sub><sup>-1</sup> = a<sub>1</sub> \| ... \| a<sub>m</sub> \| Ɛ if i = j, where δ(q<sub>i</sub>, a<sub>1</sub>) = ... = δ(q<sub>i</sub>, a<sub>m</sub>) = q<sub>j</sub>

After that, in each step the expressions R<sub>ij</sub><sup>k</sup> are computed from the previous ones by:
* R<sub>ij</sub><sup>k</sup> = R<sub>ik</sub><sup>k-1</sup>(R<sub>kk</sub><sup>k-1</sup>)\*R<sub>kj</sub><sup>k-1</sup>\|R<sub>ij</sub><sup>k-1</sup>

The Kleene’s Algorithm should be used as presented above, but with following modifications:
* Denote ∅ as `{}`
* Denote Ɛ as `eps`
* Define update rule with the additional parentheses: R<sub>ij</sub><sup>k</sup> = (R<sub>ik</sub><sup>k-1</sup>)(R<sub>kk</sub><sup>k-1</sup>)\*(R<sub>kj</sub><sup>k-1</sup>)\|(R<sub>ij</sub><sup>k-1</sup>)