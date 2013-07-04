/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.jython.gui;

import java.awt.Color;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Higlights Jython and GDA text in Swing objects.
 * <P>
 * Adaptation of HighlightingStyledDocument class written by Adam Wilson, awilson@cc.gatech.edu, as part of Jython
 * Environment for Students, Introduction to Media Computation course, Georgia Techhttp://www.gatech.edu/
 */
public class CodeDocument extends DefaultStyledDocument {
	/* Keyword text style */
	private SimpleAttributeSet keywordStyle = new SimpleAttributeSet();

	/* Environment word text style */
	// private SimpleAttributeSet environmentWordStyle = new
	// SimpleAttributeSet();
	/* Comment Style */
	private SimpleAttributeSet commentStyle = new SimpleAttributeSet();

	/* String style */
	private SimpleAttributeSet stringStyle = new SimpleAttributeSet();

	/* Default Style */
	private SimpleAttributeSet defaultStyle = new SimpleAttributeSet();

	/* Jython keywords */
	private Vector<String> keywords = new Vector<String>();

	/* Gutters */
	// private Vector gutters = new Vector();
	/* Jython environment words */
	// private Vector<String> environmentWords = new Vector<String>();
	/* Generated Regular expression for keywords */
	private Pattern keyReg = Pattern.compile("");

	/* Generated regular expression for environment words */
	// private Pattern envReg = Pattern.compile("");
	/* Regular Expression for comments */
	// private Pattern commentReg = Pattern.compile("#++[^\n]*");
	/* Regular Expression for double quote Strings */
	private Pattern doubleStringReg = Pattern.compile("\"[^\n\"]*\"");

	/* Regular Expression for single quote strings */
	private Pattern singleStringReg = Pattern.compile("'[^\n']*'");

	/* Regular Expression for string & comments */
	/* "\\\"" - why not?!? \p" */
	private Pattern stringComments = Pattern
			.compile("(#[^\n]*|\"([^\n\"\\x5c]|(\\x5c\")|(\\x5c))*\"|'([^\n'\\x5c]|(\\x5c')|(\\x5c))*')");

	/* Regular Expression to match multi-line strings */
	// private Pattern mlString = Pattern.compile("\"\"\".*\"\"\"",
	// Pattern.DOTALL);
	/* Regular Expression to match triple qoutes */
	// private Pattern triQuote = Pattern.compile("\"\"\"");
	/**
	 * 
	 */
	public CodeDocument() {
		super();

		SimpleAttributeSet commentStyle = new SimpleAttributeSet();
		commentStyle.addAttribute(StyleConstants.Foreground, new Color(0, 100, 0));
		commentStyle.addAttribute(StyleConstants.Italic, Boolean.TRUE);
		this.setCommentStyle(commentStyle);

		SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
		keywordStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
		keywordStyle.addAttribute(StyleConstants.Foreground, Color.BLUE);
		this.setKeywordStyle(keywordStyle);

		SimpleAttributeSet stringStyle = new SimpleAttributeSet();
		stringStyle.addAttribute(StyleConstants.Foreground, new Color(178, 34, 34));
		this.setStringStyle(stringStyle);

		Vector<String> jythonKeywords = new Vector<String>();

		// Jython keywords
		jythonKeywords.addElement("print");
		jythonKeywords.addElement("def");
		jythonKeywords.addElement("class");
		jythonKeywords.addElement("from");
		jythonKeywords.addElement("import");
		jythonKeywords.addElement("for");
		jythonKeywords.addElement("while");
		jythonKeywords.addElement("do");
		jythonKeywords.addElement("var");
		jythonKeywords.addElement("None");
		jythonKeywords.addElement("global");
		jythonKeywords.addElement("try");
		jythonKeywords.addElement("catch");
		jythonKeywords.addElement("except");
		jythonKeywords.addElement("if");
		jythonKeywords.addElement("else");
		jythonKeywords.addElement("elif");
		jythonKeywords.addElement("range");
		jythonKeywords.addElement("dir");
		jythonKeywords.addElement("vars");
		jythonKeywords.addElement("globals");
		jythonKeywords.addElement("locals");
		jythonKeywords.addElement("type");
		jythonKeywords.addElement("in");
		jythonKeywords.addElement("raise");
		jythonKeywords.addElement("del");
		jythonKeywords.addElement("continue");
		jythonKeywords.addElement("break");
		jythonKeywords.addElement("assert");
		jythonKeywords.addElement("reload");
		jythonKeywords.addElement("return");
		jythonKeywords.addElement("self");

		// GDA extended syntax keywords
		jythonKeywords.addElement("pos");
		jythonKeywords.addElement("inc");
		jythonKeywords.addElement("list");
		jythonKeywords.addElement("pause");
		jythonKeywords.addElement("dofs");
		jythonKeywords.addElement("scan");
		jythonKeywords.addElement("cscan");
		jythonKeywords.addElement("pscan");

		String[] keys = new String[jythonKeywords.size()];
		jythonKeywords.copyInto(keys);
		this.setKeywords(keys);
	}

	/**
	 * Overrides the default method from DefaultStyledDocument. Calls appropriate syntax highlighting code and then
	 * class super
	 * 
	 * @see javax.swing.text.Document#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offs, str, a);
		updateHighlightingInRange(offs, str.length());
	}

	/**
	 * Overrides the default method from DefaultStyledDocument. Calls appropriate syntax highlighting code and then
	 * class super.
	 * 
	 * @see javax.swing.text.AbstractDocument#fireRemoveUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	protected void fireRemoveUpdate(DocumentEvent e) {
		int offset = e.getOffset();
		updateHighlightingInRange(offset - 1, 0);
		super.fireRemoveUpdate(e);
	}

	/**
	 * Looks at a given range of text in a document and highlights it according to keywords, environment, strings, and
	 * comments.
	 * 
	 * @param offset
	 *            Where in the document the change started
	 * @param length
	 *            The length of change measured from the offset
	 */
	public void updateHighlightingInRange(int offset, int length) {
		try {
			// int start = getLineStart(textAll, offset);
			// int end = getLineEnd(textAll, offset + length);

			Element defaultElement = getDefaultRootElement();
			int line = defaultElement.getElementIndex(offset);
			int lineend = defaultElement.getElementIndex(offset + length);
			int start = defaultElement.getElement(line).getStartOffset();
			int end = defaultElement.getElement(lineend).getEndOffset();

			String text = getText(start, end - start);
			setCharacterAttributes(start, end - start, defaultStyle, true);

			// Do Block Highlighting:

			// Find and highlight keywords:
			Matcher m = keyReg.matcher(text);
			while (m.find())
				setCharacterAttributes(start + m.start(), m.end() - m.start(), keywordStyle, true);

			// Find and highlight keywords:
			// m = envReg.matcher(text);
			// while (m.find())
			// setCharacterAttributes(start + m.start(), m.end() -
			// m.start(),
			// environmentWordStyle, true);

			// Find and highlight Comments and strings:
			m = stringComments.matcher(text);
			while (m.find()) {
				if (text.charAt(m.start()) == '#')
					setCharacterAttributes(start + m.start(), m.end() - m.start(), commentStyle, true);
				// if (text.charAt(m.start()) == '\'' || text.charAt(m.start())
				// ==
				// '"')
				// setCharacterAttributes(start + m.start(), m.end() -
				// m.start(),
				// stringStyle, true);
			}

			m = doubleStringReg.matcher(text);
			while (m.find()) {
				if (text.charAt(m.start()) == '\'' || text.charAt(m.start()) == '"')
					setCharacterAttributes(start + m.start(), m.end() - m.start(), stringStyle, true);
			}

			m = singleStringReg.matcher(text);
			while (m.find()) {
				if (text.charAt(m.start()) == '\'' || text.charAt(m.start()) == '"')
					setCharacterAttributes(start + m.start(), m.end() - m.start(), stringStyle, true);
			}
			// Matches Multi-line strings starting with triple quotes:
			/*
			 * m = mlString.matcher(textAll); while(m.find()) setCharacterAttributes(m.start(), m.end() - m.start(),
			 * stringStyle, true);
			 */
		} catch (Exception e) {
		}
	}

	/**
	 * Takes in an index and finds the offset of the end of the line
	 * 
	 * @param text
	 *            The text to find the end of the line in
	 * @param offset
	 *            An index of a character on that line.
	 * @return int
	 */
	/*
	 * private int getLineEnd(String text, int offset) { while (offset < text.length()) { if (text.charAt(offset) ==
	 * '\n') return (offset); offset++; } return offset; }
	 */
	/**
	 * Looks at a location in the given document and determines if that location is inside a string. Supports """ for
	 * multi-line strings.
	 * 
	 * @param offset
	 *            The location to check for string-ness
	 * @return True for is a string, false for is not a string
	 */
	/*
	 * private boolean isString(int offset) { return false; }
	 */
	/**
	 * Looks at a location inside a document and determines if it is a comment.
	 * 
	 * @param offset
	 *            The location to check for stringness
	 * @return True for is a comment, false for is not a comment
	 */
	/*
	 * private boolean isComment(int offset) { return false; }
	 */
	/**
	 * Sets a collection of keywords to highlight.
	 * 
	 * @param words
	 *            An array of all the words
	 */
	public void setKeywords(String[] words) {
		keywords.clear();
		for (int i = 0; i < words.length; i++) {
			keywords.add(words[i]);
		}
		compileKeywords();
	}

	/**
	 * Sets a collection of environment words to highlight.
	 * 
	 * @param words
	 *            An array of all the words
	 */
	/*
	 * public void setEnvironmentWords(String[] words) { environmentWords.clear(); for (int i = 0; i < words.length;
	 * i++) { environmentWords.add(words[i]); } compileEnvironmentWords(); }
	 */
	/**
	 * Adds a keyword to the Vector of keywords.
	 * 
	 * @param word
	 *            The word to add
	 */
	public void addKeyword(String word) {
		keywords.add(word);
		compileKeywords();
	}

	/**
	 * Adds an environment word to the Vector of environment words.
	 * 
	 * @param word
	 *            The word to add
	 */
	/*
	 * public void addEnvironmentWord(String word) { environmentWords.add(word); compileEnvironmentWords(); }
	 */
	/**
	 * Sets the style of text to use for keywords
	 * 
	 * @param style
	 *            The new text style
	 */
	public void setKeywordStyle(SimpleAttributeSet style) {
		keywordStyle = style;
	}

	/**
	 * Sets the style of text to use for environment words
	 * 
	 * @param style
	 *            The new text style
	 */
	/*
	 * public void setEnvironmentWordStyle(SimpleAttributeSet style) { environmentWordStyle = style; }
	 */
	/**
	 * Sets the style of text to use for comments
	 * 
	 * @param style
	 *            The new text style
	 */
	public void setCommentStyle(SimpleAttributeSet style) {
		commentStyle = style;
	}

	/**
	 * Sets the style of text to use for strings
	 * 
	 * @param style
	 *            The new text style
	 */
	public void setStringStyle(SimpleAttributeSet style) {
		stringStyle = style;
	}

	/**
	 * Sets the default style of text to use
	 * 
	 * @param style
	 *            The new text style
	 */
	public void setDefaultStyle(SimpleAttributeSet style) {
		defaultStyle = style;
	}

	/*
	 * Recompiles the regular expression used for matching key words. Takes the collection of keywords and generates a
	 * regular expression string. It then compiles that string into the Pattern class and stores it in keyReg. Example:
	 * if the keywords were "if" and "for", the regular expression would be: "\W(if|for)\W". The \W isolate the keywords
	 * by non-word characters.
	 */
	private void compileKeywords() {
		String exp = new String();
		exp = "\\b("; // Start the expression to match non-word characters,
		// i.e. [^a-zA-Z0-9], and then start the OR block.
		for (int i = 0; i < keywords.size(); i++) {
			if (i == 0)
				exp = exp + (keywords.elementAt(i)).trim();
			exp = exp + "|" + (keywords.elementAt(i)).trim();
		}
		exp = exp + ")\\b";
		keyReg = Pattern.compile(exp);
	}

	/**
	 * Recompiles the regular expression used for matching environment words. Takes the collection of environment words
	 * and generates a regular expression string. It then compiles that string into the Pattern class and stores it in
	 * envReg. Example: if the envwords were "if" and "for", the regular expression would be: "\W(if|for)\W". The \W
	 * isolate the envwords by non-word characters.
	 */
	/*
	 * private void compileEnvironmentWords() { String exp = new String(); exp = "\\b("; // Start the expression to
	 * match non-word characters, // i.e. [^a-zA-Z0-9], and then start the OR block. for (int i = 0; i <
	 * environmentWords.size(); i++) { if (i == 0) exp = exp + (environmentWords.elementAt(i)).trim(); exp = exp + "|" +
	 * (environmentWords.elementAt(i)).trim(); } exp = exp + ")\\b"; envReg = Pattern.compile(exp); }
	 */
}
