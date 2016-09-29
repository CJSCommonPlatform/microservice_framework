package uk.gov.justice.services.test.utils.core.random;

import java.util.Random;

/**
 * Provides template for random value generation
 * for a given type T
 * @param <T> random value of T will be generated by the implementing class
 */
public abstract class Generator<T> {
    final Random RANDOM = new java.util.Random();

    public abstract T next();
}
