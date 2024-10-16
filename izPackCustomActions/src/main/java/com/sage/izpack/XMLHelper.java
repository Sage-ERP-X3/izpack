package com.sage.izpack;

import org.w3c.dom.Node;


/**
 * A Collection of convenient XML-Helper Methods and Constants
 *
 * @author marc.eppelmann&#064;gmx.de
 * @version $Revision: 1.1 $
 */
public class XMLHelper
{
    // Static fields/initializers *********************************************************

    /**
     * YES   = "YES"
     */
    public final static String YES = "YES";

    /**
     * NO = "NO"
     */
    public final static String NO = "NO";

    /**
     * TRUE = "TRUE"
     */
    public final static String TRUE = "TRUE";

    /**
     * FALSE = "FALSE"
     */
    public final static String FALSE = "FALSE";

    /**
     * ON = "ON"
     */
    public final static String ON = "ON";

    /**
     * OFF = "OFF"
     */
    public final static String OFF = "OFF";

    /**
     * _1 = "1"
     */
    public final static String _1 = "1";

    /**
     * _0 = "0"
     */
    public final static String _0 = "0";

    //~ Constructors ***********************************************************************

    /**
     * Creates a new XMLHelper object.
     */
    public XMLHelper()
    {
        super();
    }


    
    /**
     * Removes text nodes that only contains whitespace. The conditions for
     * removing text nodes, besides only containing whitespace, are: If the
     * parent node has at least one child of any of the following types, all
     * whitespace-only text-node children will be removed: - ELEMENT child -
     * CDATA child - COMMENT child
     * 
     * The purpose of this is to make the format() method (that use a
     * Transformer for formatting) more consistent regarding indenting and line
     * breaks.
     */
    public static void cleanEmptyTextNodes(Node parentNode) {
        boolean removeEmptyTextNodes = false;
        Node childNode = parentNode.getFirstChild();
        while (childNode != null) {
            removeEmptyTextNodes |= checkNodeTypes(childNode);
            childNode = childNode.getNextSibling();
        }

        if (removeEmptyTextNodes) {
            removeEmptyTextNodes(parentNode);
        }
    }

    private static void removeEmptyTextNodes(Node parentNode) {
        Node childNode = parentNode.getFirstChild();
        while (childNode != null) {
            // grab the "nextSibling" before the child node is removed
            Node nextChild = childNode.getNextSibling();

            short nodeType = childNode.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                boolean containsOnlyWhitespace = childNode.getNodeValue()
                        .trim().isEmpty();
                if (containsOnlyWhitespace) {
                    parentNode.removeChild(childNode);
                }
            }
            childNode = nextChild;
        }
    }

    private static boolean checkNodeTypes(Node childNode) {
        short nodeType = childNode.getNodeType();

        if (nodeType == Node.ELEMENT_NODE) {
            cleanEmptyTextNodes(childNode); // recurse into subtree
        }

        if (nodeType == Node.ELEMENT_NODE
                || nodeType == Node.CDATA_SECTION_NODE
                || nodeType == Node.COMMENT_NODE) {
            return true;
        } else {
            return false;
        }
    }    
    
}
