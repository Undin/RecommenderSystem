package weka.attributeSelection;

import weka.core.Instances;

import java.util.stream.IntStream;

/**
 * Created by warrior on 02.05.15.
 */
public class FixedTabuSearch extends TabuSearch {
    @Override
    public int[] search(ASEvaluation ASEval, Instances data) throws Exception {
        int[] selectedAttributes = super.search(ASEval, data);
        return IntStream.of(selectedAttributes)
                .filter(i -> i != data.classIndex())
                .toArray();
    }
}
