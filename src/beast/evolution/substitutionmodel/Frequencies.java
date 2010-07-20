
/*
 * File Frequencies.java
 *
 * Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
 *
 * This file is part of BEAST2.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Input;
import beast.core.Plugin;
import beast.core.Input.Validate;
import beast.evolution.alignment.Alignment;

@Description("Calculates empirical frequencies of characters in sequence data")
public class Frequencies extends Plugin {
	public Input<Alignment> m_data = new Input<Alignment>("data", "Sequence data for which frequencies are calculated", Validate.REQUIRED);

	@Override
	public void initAndValidate() throws Exception {
		if (m_data.get() == null) {
			throw new Exception("data input is not specified");
		}
		calcFrequencies();
	}

	double [] m_fFreqs;
	public double [] getFreqs() {
		return m_fFreqs;
	}

	void calcFrequencies() {
		Alignment alignment = m_data.get();
		m_fFreqs = new double[alignment.getMaxStateCount()];
		for (int i = 0; i < alignment.getPatternCount(); i++) {
			int [] nPattern = alignment.getPattern(i);
			int nWeight = alignment.getPatternWeight(i);
			for (int iValue : nPattern) {
				if (iValue != m_fFreqs.length) { // ignore unknowns
					m_fFreqs[iValue] += nWeight;
				}
			}
		}
		// normalize
		double fSum = 0;
		for (double f : m_fFreqs) {
			fSum += f;
		}
		for (int i = 0; i < m_fFreqs.length; i++) {
			m_fFreqs[i] /= fSum;
		}
	} // calcFrequencies


	public boolean isDirty() {
		return false;
	}

} // class Frequencies
