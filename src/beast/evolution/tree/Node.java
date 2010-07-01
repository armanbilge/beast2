
/*
 * File Node.java
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
package beast.evolution.tree;

import beast.core.*;

import java.util.List;

@Description("Nodes in building binary beast.tree data structure.")
public class Node extends Plugin {
	/** label nr of node, only used when this is a leaf **/
	protected int m_iLabel;
	/** height of this node. */
	protected double m_fHeight = Double.MAX_VALUE;
	/** list of children of this node **/
	public Node m_left;
	public Node m_right;
	/** parent node in the beast.tree, null if root **/
	Node m_Parent = null;
	/** status of this node after an operation is performed on the state **/
	int m_bIsDirty = State.IS_CLEAN;
	/** meta-data contained in square brackets in Newick **/
	public String m_sMetaData;

	public int getNr() {return m_iLabel;}
	public void setNr(int iLabel) {m_iLabel = iLabel;}

	public double getHeight() {return m_fHeight;}
	public void setHeight(double fHeight) {
		m_fHeight = fHeight;
		m_bIsDirty |= State.IS_DIRTY;
		if (!isLeaf()) {
			m_left.m_bIsDirty |= State.IS_DIRTY;
			m_right.m_bIsDirty |= State.IS_DIRTY;
		}
	}

	/** length of branch in the beast.tree **/
	public double getLength() {
		if (isRoot()) {
			return 0;
		} else {
			return getParent().m_fHeight - m_fHeight;
		}
	}

	public int isDirty() {return m_bIsDirty;}
	public void makeDirty(int nDirty) {
		m_bIsDirty |= nDirty;
	}
	public void makeAllDirty(int nDirty) {
		m_bIsDirty = nDirty;
		if (!isLeaf()) {
			m_left.makeAllDirty(nDirty);
			m_right.makeAllDirty(nDirty);
		}
	}


	/** return parent node, or null if this is root **/
	public Node getParent() {
		return m_Parent;
	}
	public void setParent(Node parent) {
		m_Parent = parent;
	}

	/** check if current node is root node **/
	public boolean isRoot() {
		return m_Parent == null;
	}

	/** check if current node is a leaf node **/
	public boolean isLeaf() {
		return m_left == null;
	}

	/** count number of nodes in beast.tree, starting with current node **/
	public int getNodeCount() {
		if (isLeaf()) {
			return 1;
		}
		return 1 + m_left.getNodeCount() + m_right.getNodeCount();
	}

	 /**
	 * print beast.tree in Newick format, without any length or meta data
	 * information
	 **/
	public String toShortNewick() {
		StringBuffer buf = new StringBuffer();
		if (m_left != null) {
			buf.append("(");
			buf.append(m_left.toShortNewick());
			buf.append(',');
			buf.append(m_right.toShortNewick());
			buf.append(")");
		} else {
//			if (m_sID != null) {
//				buf.append(m_sID);
//			} else {
				buf.append(m_iLabel);
//			}
		}
//		buf.append("["+m_iLabel+"]");
		buf.append(":" + String.format("%3.3f",getLength()));
		return buf.toString();
	}

	/**
	 * print beast.tree in long Newick format, with all length and meta data
	 * information
	 **/
	public String toNewick(List<String> sLabels) {
		StringBuffer buf = new StringBuffer();
		if (m_left != null) {
			buf.append("(");
			buf.append(m_left.toNewick(sLabels));
			buf.append(',');
			buf.append(m_right.toNewick(sLabels));
			buf.append(")");
		} else {
			if (sLabels == null) {
				buf.append(m_iLabel);
			} else {
				buf.append(sLabels.get(m_iLabel));
			}
		}
		buf.append(getNewickMetaData());
		buf.append(":" + getLength());
		return buf.toString();
	}
	public String getNewickMetaData() {
		if (m_sMetaData != null) {
			return '[' + m_sMetaData + ']';
		}
		return "";
	}

	/**
	 * print beast.tree in long Newick format, with all length and meta data
	 * information, but with leafs labelled with their names
	 **/
	public String toString(List<String> sLabels) {
		StringBuffer buf = new StringBuffer();
		if (m_left != null) {
			buf.append("(");
			buf.append(m_left.toString(sLabels));
			buf.append(',');
			buf.append(m_right.toString(sLabels));
			buf.append(")");
		} else {
			buf.append(sLabels.get(m_iLabel));
		}
		if (m_sMetaData != null) {
			buf.append('[');
			buf.append(m_sMetaData);
			buf.append(']');
		}
		buf.append(":" + getLength());
		return buf.toString();
	}

	public String toString() {
		return toShortNewick();
	}

	/**
	 * sorts nodes in children according to lowest numbered label in subtree
	 **/
	public int sort() {
		if (m_left != null) {
			int iChild1 = m_left.sort();
			int iChild2 = m_right.sort();
			if (iChild1 > iChild2) {
				Node tmp = m_left;
				m_left = m_right;
				m_right = tmp;
				return iChild2;
			}
			return iChild1;
		}
		// this is a leaf node, just return the label nr
		return m_iLabel;
	} // sort

	/** during parsing, leaf nodes are numbered 0...m_nNrOfLabels-1
	 * but internal nodes are left to zero. After labeling internal
	 * nodes, m_iLabel uniquely identifies a node in a beast.tree.
	 */
	public int labelInternalNodes(int iLabel) {
		if (isLeaf()) {
			return iLabel;
		} else {
			iLabel = m_left.labelInternalNodes(iLabel);
			iLabel = m_right.labelInternalNodes(iLabel);
			m_iLabel = iLabel++;
		}
		return iLabel;
	} // labelInternalNodes

	/** create deep copy **/
	public Node copy() {
		Node node = new Node();
		node.m_fHeight = m_fHeight;
		node.m_iLabel = m_iLabel;
		node.m_sMetaData = m_sMetaData;
		node.m_Parent = null;
		node.m_sID = m_sID;
		if (m_left != null) {
			node.m_left = m_left.copy();
			node.m_right = m_right.copy();
			node.m_left.m_Parent = node;
			node.m_right.m_Parent = node;
		}
		return node;
	} // copy

	public void setMetaData(String sPattern, double fValue) {
	}
	public double getMetaData(String sPattern) {
		return 0;
	}

	/** scale height of this node and all its descendants **/
	public void scale(double fScale) {
		m_fHeight *= fScale;
		m_bIsDirty |= State.IS_DIRTY;
		if (!isLeaf()) {
			m_left.scale(fScale);
			m_right.scale(fScale);
		}
	}

} // class Node