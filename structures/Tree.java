package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file
	 */
	public void build() {
		Stack<TagNode> tags = new Stack<TagNode>();
		sc.nextLine();
		root = new TagNode("html", null, null);
		tags.push(root); // pushes initial <html> tag into stack
		
		while(sc.hasNextLine()) {
			String str = sc.nextLine();
			Boolean isTag = false;
			if(str.charAt(0) == '<') {
				if(str.charAt(1) == '/') {
					tags.pop();
					continue;
				} else {
					str = str.replace("<", "");
					str = str.replace(">", "");
					isTag = true;
				}
			}
			TagNode temp = new TagNode(str, null, null);
			if(tags.peek().firstChild == null) {
				tags.peek().firstChild = temp; 
			} else {
				TagNode ptr = tags.peek().firstChild;
				while(ptr.sibling != null) {
					ptr = ptr.sibling;
				}
				ptr.sibling = temp;
			}
			if(isTag) tags.push(temp);
		}
	}
	
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		replaceTag(root, oldTag, newTag);
	}
	
	private void replaceTag(TagNode root, String oldTag, String newTag) {
		if(root == null) return;
		if(root.tag.equals(oldTag) && root.firstChild != null) { // root.firstChild must not be null to be a tag
			root.tag = newTag;
		}
		replaceTag(root.sibling, oldTag, newTag);
		replaceTag(root.firstChild, oldTag, newTag);
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */

	public void boldRow(int row) { 
		TagNode table = findTable(root);
		TagNode tr = table.firstChild;
		for(int r=1; r != row; r++) { // finds row to bold, null pointer if row does not exist?!?!
			tr = tr.sibling;
		}
		for(TagNode td = tr.firstChild; td != null; td = td.sibling) { // adds <b> tags under <td> tags
			TagNode b = new TagNode("b", td.firstChild, null);
			td.firstChild = b;
		}
	}
	
	// Recursively finds table TagNode in tree
	private TagNode findTable(TagNode root) { 
		if(root == null) return null; 
		if(root.tag.equals("table")) return root; 
		TagNode s = findTable(root.sibling);
		TagNode f = findTable(root.firstChild);
		if(s != null) return s; 
		if(f != null) return f; 
		return null;
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) { 
		if((tag.equals("p") || tag.equals("em") || tag.equals("b"))) removeTag1(root, tag);
		if((tag.equals("ol") || tag.equals("ul"))) removeTag2(root, tag);
	}
	
	private void removeTag1(TagNode root, String tag) { // tag to be removed is <p>, <em>, or <b>
		if(root == null) return;
		if(root.tag.equals(tag) && root.firstChild != null) {
			root.tag = root.firstChild.tag;
			if(root.firstChild.sibling != null) {
				TagNode ptr = null;
				for(ptr = root.firstChild; ptr.sibling != null; ptr = ptr.sibling); 
				ptr.sibling = root.sibling;
				root.sibling = root.firstChild.sibling;
			}
			root.firstChild = root.firstChild.firstChild;
		}
		removeTag1(root.firstChild, tag); 
		removeTag1(root.sibling, tag);
	}
	
	private void removeTag2(TagNode root, String tag) { // tag to be removed is <ol> or <ul>
		if(root == null) return;
		if(root.tag.equals(tag) && root.firstChild != null) {
			root.tag = "p";
			TagNode ptr = null;
			for(ptr = root.firstChild; ptr.sibling != null; ptr = ptr.sibling) ptr.tag = "p"; 
			// ^ changes all <li> tags to <p> tags and finds last <li> TagNode
			ptr.tag = "p";
			ptr.sibling = root.sibling;
			root.sibling = root.firstChild.sibling;
			root.firstChild = root.firstChild.firstChild;
		}
		removeTag2(root.firstChild, tag); 
		removeTag2(root.sibling, tag);
	}
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		if(!word.matches("[a-zA-Z]{"+word.length()+"}")) return; // returns if word is not alphabetic
		if(tag.equals("em") || tag.equals("b")) addTag(root, word.toLowerCase(), tag); // tags can only be <em> or <b>	
	}

	private void addTag(TagNode root, String word, String tag) {
		if(root == null) return; 
		addTag(root.firstChild, word, tag);
		addTag(root.sibling, word, tag);
		if(root.firstChild == null) {	// root.firstChild must be null to be text
			while(root.tag.toLowerCase().contains(word)) {
				String[] text = root.tag.split(" ");
				Boolean found_Word_Regex = false;
				String taggedWord = "";
				StringBuilder sb = new StringBuilder(root.tag.length());
				int i = 0;
				for(i=0; i<text.length; i++) {
					if(text[i].toLowerCase().matches(word+"[.?!,]?")) {
						found_Word_Regex = true;
						taggedWord = text[i];
						for(int x=i+1; x<text.length; x++) sb.append(text[x]+" ");
						break;
					}
				}
				if(!found_Word_Regex) return;
				
				String restOfTag = sb.toString().trim(); // taggedWord+" "+restOfTag == root.tag
				if(i == 0) { // word is at beginning of text
					root.firstChild = new TagNode(taggedWord, null, null);
					root.tag = tag;
					if(!restOfTag.equals("")) { 
						root.sibling = new TagNode(restOfTag, null, root.sibling);
						root = root.sibling;
					}
				} else { // word is at middle or end of text
					TagNode taggedWordNode = new TagNode(taggedWord, null, null);
					TagNode newTag = new TagNode(tag, taggedWordNode, root.sibling);
					root.sibling = newTag;
					root.tag = root.tag.replaceFirst(" "+taggedWord, "");
					if(!restOfTag.equals("")) { // word is in middle of text
						root.tag = root.tag.replace(restOfTag, "");
						newTag.sibling = new TagNode(restOfTag, null, newTag.sibling);
						root = newTag.sibling;
					}
				}
			} 
		}
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
}
