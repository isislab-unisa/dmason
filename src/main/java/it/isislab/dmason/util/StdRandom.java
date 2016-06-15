/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.util;



import java.util.Random;

// TODO: Auto-generated Javadoc
/*************************************************************************
 *  Compilation:  javac StdRandom.java
 *  Execution:    java StdRandom
 *
 *  A library of static methods to generate random numbers from
 *  different distributions (bernoulli, uniform, gaussian,
 *  discrete, and exponential). Also includes a method for
 *  shuffling an array.
 *
 *  % java StdRandom 5
 *  90 26.36076 false 8.79269 0
 *  13 18.02210 false 9.03992 1
 *  58 56.41176 true  8.80501 0
 *  29 16.68454 false 8.90827 0
 *  85 86.24712 true  8.95228 0
 *
 *
 *  Remark
 *  ------
 *    - Uses Math.random() which generates a pseudorandom real number
 *      in [0, 1)
 *
 *    - This library does not allow you to set the pseudorandom number
 *      seed. See java.util.Random.
 *
 *    - See http://www.honeylocust.com/RngPack/ for an industrial
 *      strength random number generator in Java.
 *
 *************************************************************************/


/**
 * Compilation:  javac StdRandom.java Execution:    java StdRandom A library of static methods to generate random numbers from different distributions (bernoulli, uniform, gaussian, discrete, and exponential). Also includes a method for shuffling an array. % java StdRandom 5 90 26.36076 false 8.79269 0 13 18.02210 false 9.03992 1 58 56.41176 true  8.80501 0 29 16.68454 false 8.90827 0 85 86.24712 true  8.95228 0 Remark ------ - Uses Math.random() which generates a pseudorandom real number in [0, 1) - This library does not allow you to set the pseudorandom number seed. See java.util.Random. - See http://www.honeylocust.com/RngPack/ for an industrial strength random number generator in Java.
 */
public class StdRandom {
	
	/**
	 * The random generator.
	 * 
	 */
	static Random randomGenerator = new Random();

	/**
	 * Gets the random generator.
	 * @return  the random generator
	 */
	public static Random getRandomGenerator() {
		return randomGenerator;
	}

	/**
	 * Sets the random generator.
	 * @param randomGenerator  the new random generator
	 */ 
	 
	public static void setRandomGenerator(Random randomGenerator) {
		StdRandom.randomGenerator = randomGenerator;
	}


	/**
	 * Return real number by an harmonic distribution in [a, b).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double
	 */
	public static double harmonic(int a, int b) {
		double range = b - a - 1;
		double power = Math.pow(2.0, range);
		double loga = Math.log10(uniform(1, power + 1)) / Math.log10(2.0);
		double inver = range - loga;
		
		return (int) (a + inver);
	}

	/**
	 * Return real number uniformly in [0, 1).
	 *
	 * @return the double
	 */
	public static double uniform() {
		return randomGenerator.nextDouble();
	}

	/**
	 * Return real number uniformly in [a, b).
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double
	 */
	public static double uniform(double a, double b) {
		return a + uniform() * (b-a);
	}

	/**
	 * Return an integer uniformly between 0 and N-1.
	 *
	 * @param N the n
	 * @return the int
	 */
	public static int uniform(int N) {
		return (int) (uniform() * N);
	}

	/**
	 * Return a boolean, which is true with probability p, and false otherwise.
	 *
	 * @param p the p
	 * @return true, if successful
	 */
	public static boolean bernoulli(double p) {
		return uniform() < p;
	}

	/**
	 * Return a boolean, which is true with probability .5, and false otherwise.
	 *
	 * @return true, if successful
	 */
	public static boolean bernoulli() {
		return bernoulli(0.5);
	}

	/**
	 * Return a real number with a standard Gaussian distribution.
	 *
	 * @return the double
	 */
	public static double gaussian() {
		// use the polar form of the Box-Muller transform
		double r, x, y;
		do {
			x = uniform(-1.0, 1.0);
			y = uniform(-1.0, 1.0);
			r = x*x + y*y;
		} while (r >= 1 || r == 0);
		return x * Math.sqrt(-2 * Math.log(r) / r);

		// Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
		// is an independent random gaussian
	}

	/**
	 * Return a real number from a gaussian distribution with given mean and stddev.
	 *
	 * @param mean the mean
	 * @param stddev the stddev
	 * @return the double
	 */
	public static double gaussian(double mean, double stddev) {
		return mean + stddev * gaussian();
	}

	/**
	 * Return an integer with a geometric distribution with mean 1/p.
	 *
	 * @param p the p
	 * @return the int
	 */
	public static int geometric(double p) {
		// using algorithm given by Knuth
		return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
	}

	/**
	 * Return an integer with a Poisson distribution with mean lambda.
	 *
	 * @param lambda the lambda
	 * @return the int
	 */
	public static int poisson(double lambda) {
		// using algorithm given by Knuth
		// see http://en.wikipedia.org/wiki/Poisson_distribution
		int k = 0;
		double p = 1.0;
		double L = Math.exp(-lambda);
		do {
			k++;
			p *= uniform();
		} while (p >= L);
		return k-1;
	}

	/**
	 * Return a real number with a Pareto distribution with parameter alpha.
	 *
	 * @param alpha the alpha
	 * @return the double
	 */
	public static double pareto(double alpha) {
		return Math.pow(1 - uniform(), -1.0/alpha) - 1.0;
	}

	/**
	 * Return a real number with a Cauchy distribution.
	 *
	 * @return the double
	 */
	public static double cauchy() {
		return Math.tan(Math.PI * (uniform() - 0.5));
	}

	/**
	 * Return a number from a discrete distribution: i with probability a[i].
	 *
	 * @param a the a
	 * @return the int
	 */
	public static int discrete(double[] a) {
		// precondition: sum of array entries equals 1
		double r = uniform();
		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum = sum + a[i];
			if (sum >= r) return i;
		}
		assert (false);
		return -1;
	}

	/**
	 * Return a real number from an exponential distribution with rate lambda.
	 *
	 * @param lambda the lambda
	 * @return the double
	 */
	public static double exponential(double lambda) {
		return -Math.log(1 - uniform()) / lambda;
	}

	/**
	 * Rearrange the elements of an array in random order.
	 *
	 * @param a the a
	 */
	public static void shuffle(Object[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int r = i + uniform(N-i);     // between i and N-1
			Object temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}

	/**
	 * Rearrange the elements of a double array in random order.
	 *
	 * @param a the a
	 */
	public static void shuffle(double[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int r = i + uniform(N-i);     // between i and N-1
			double temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}

	/**
	 * Rearrange the elements of an int array in random order.
	 *
	 * @param a the a
	 */
	public static void shuffle(int[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int r = i + uniform(N-i);     // between i and N-1
			int temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}


	/**
	 * Rearrange the elements of the subarray a[lo..hi] in random order.
	 *
	 * @param a the a
	 * @param lo the lo
	 * @param hi the hi
	 */
	public static void shuffle(Object[] a, int lo, int hi) {
		if (lo < 0 || lo > hi || hi >= a.length)
			throw new RuntimeException("Illegal subarray range");
		for (int i = lo; i <= hi; i++) {
			int r = i + uniform(hi-i+1);     // between i and hi
			Object temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}

	/**
	 * Rearrange the elements of the subarray a[lo..hi] in random order.
	 *
	 * @param a the a
	 * @param lo the lo
	 * @param hi the hi
	 */
	public static void shuffle(double[] a, int lo, int hi) {
		if (lo < 0 || lo > hi || hi >= a.length)
			throw new RuntimeException("Illegal subarray range");
		for (int i = lo; i <= hi; i++) {
			int r = i + uniform(hi-i+1);     // between i and hi
			double temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}

	/**
	 * Rearrange the elements of the subarray a[lo..hi] in random order.
	 *
	 * @param a the a
	 * @param lo the lo
	 * @param hi the hi
	 */
	public static void shuffle(int[] a, int lo, int hi) {
		if (lo < 0 || lo > hi || hi >= a.length)
			throw new RuntimeException("Illegal subarray range");
		for (int i = lo; i <= hi; i++) {
			int r = i + uniform(hi-i+1);     // between i and hi
			int temp = a[i];
			a[i] = a[r];
			a[r] = temp;
		}
	}


	/**
	 * Unit test.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		int N = 10;

		for (int i = 0; i < N; i++) {
			System.out.println(exponential(0.15));			
//			System.out.println(harmonic(2, 20));
//			System.out.printf("%8.5f ", uniform(10.0, 99.0));
//			System.out.printf("%5b "  , bernoulli(.5));
//			System.out.printf("%7.5f ", gaussian(9.0, .2));
//			System.out.printf("%2d "  , discrete(t));
//			System.out.println();
		}
	}

}