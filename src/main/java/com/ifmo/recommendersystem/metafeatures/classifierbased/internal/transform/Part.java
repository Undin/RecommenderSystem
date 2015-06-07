package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Transform;
import weka.core.Instances;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by warrior on 04.06.15.
 */
public abstract class Part implements Transform {

    @Override
    public Instances transform(Instances instances) {
        int number = resultInstanceNumber(instances);
        List<Integer> indexes = IntStream.range(0, instances.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(indexes);
        Instances newInstances = new Instances(instances, number);
        indexes.stream()
                .map(instances::get)
                .forEach(newInstances::add);
        return newInstances;
    }

    protected abstract int resultInstanceNumber(Instances instances);
}
