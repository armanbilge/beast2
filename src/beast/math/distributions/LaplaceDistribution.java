package beast.math.distributions;

import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.MathException;

import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.math.distributions.LogNormalDistributionModel.LogNormalImpl;

@Description("Laplace distribution.    f(x|\\mu,b) = \\frac{1}{2b} \\exp \\left( -\\frac{|x-\\mu|}{b} \\right)" +
        "The probability density function of the Laplace distribution is also reminiscent of the normal distribution; " +
        "however, whereas the normal distribution is expressed in terms of the squared difference from the mean ?, " +
        "the Laplace density is expressed in terms of the absolute difference from the mean. Consequently the Laplace " +
        "distribution has fatter tails than the normal distribution.")
public class LaplaceDistribution extends ParametricDistribution {
    public Input<RealParameter> muInput = new Input<RealParameter>("mu", "location parameter, defaults to 0");
    public Input<RealParameter> scaleInput = new Input<RealParameter>("scale", "scale parameter, defaults to 1");

    // the mean parameter
    double mu;
    // the scale parameter
    double scale;
    // the maximum density
    double c;
    LaplaceImpl dist = new LaplaceImpl();

    @Override
    public void initAndValidate() {
        refresh();
    }

    /**
     * make sure internal state is up to date *
     */
    void refresh() {

        if (muInput.get() == null) {
            mu = 0;
        } else {
            mu = muInput.get().getValue();
        }
        if (scaleInput.get() == null || scaleInput.get().getValue()<=0.0) {
            scale = 1;
        } else {
            scale = scaleInput.get().getValue();
        }

        //Normalizing constant
        c = 1.0 / (2.0 * scale);
    }

    @Override
    public ContinuousDistribution getDistribution() {
        refresh();
        return dist;
    }

    class LaplaceImpl implements ContinuousDistribution {

        @Override
        public double cumulativeProbability(double x) throws MathException {
            // =0.5\,[1 + \sgn(x-\mu)\,(1-\exp(-|x-\mu|/b))].
            if (x == mu) {
                return 0.5;
            } else {
                return (0.5) * (1 + ((x - mu) / Math.abs(x - mu)) * (1 - Math.exp(-Math.abs(x - mu) / scale)));
            }
        }

        @Override
        public double cumulativeProbability(double x0, double x1) throws MathException {
            return cumulativeProbability(x1) - cumulativeProbability(x0);
        }

        @Override
        public double inverseCumulativeProbability(double p) throws MathException {
            //     \mu - b\,\sgn(p-0.5)\,\ln(1 - 2|p-0.5|).
            return mu - scale * Math.signum(p - 0.5) * Math.log(1.0 - 2.0 * Math.abs(p - 0.5));
        }

        @Override
        public double density(double fX) {
            // f(x|\mu,b) = \frac{1}{2b} \exp \left( -\frac{|x-\mu|}{b} \right) \,\!
            return c * Math.exp(-Math.abs(fX - mu) / scale);
        }

        @Override
        public double logDensity(double fX) {
            return Math.log(c) - (Math.abs(fX - mu) / scale);
        }
    } // class LaplaceImpl

} // class