package com.ifmo.recommendersystem.metafeatures.classifierbased.internal.transform;

import com.ifmo.recommendersystem.metafeatures.classifierbased.Transform;
import com.ifmo.recommendersystem.utils.InstancesUtils;
import weka.core.Instances;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by warrior on 04.06.15.
 */
public abstract class Part implements Transform {

    private Random random = new Random();

    @Override
    public Instances transform(Instances instances) {
        Set<Integer> indexes = new HashSet<>();
        int number = resultAttributeNumber(instances);
        while (indexes.size() < number) {
            int index = random.nextInt(instances.size() - 1);
            if (!indexes.contains(index)) {
                indexes.add(index);
            }
        }
        return InstancesUtils.removeAttributes(instances, indexes.stream().mapToInt(i -> i).toArray(), true);
    }

    protected abstract int resultAttributeNumber(Instances instances);
}
