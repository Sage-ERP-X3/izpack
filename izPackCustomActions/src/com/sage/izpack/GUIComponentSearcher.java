package com.sage.izpack;

import java.awt.Container;
import java.awt.Component;
import java.lang.reflect.ParameterizedType;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * Browsing of the GUI components tree to find the JTextPane instance having the
 * index N.
 * 
 * 
 * Sample:
 * 
 * <pre>
    IsTextPane=[false] Component=[JScrollPane][null                                    ]
	IsTextPane=[false] Component=[JViewport  ][null                                    ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[false] Component=[JLabel     ][Contrle des prrequis de compilation   ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[true ] Component=[JTextPane  ][Informations:                           ]
	IsTextPane=[true ] Component=[JTextPane  ][Vrification de la prsence des outils d]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[false] Component=[JPanel     ][null                                    ]
	IsTextPane=[true ] Component=[JTextPane  ][Resultat:                               ]
	IsTextPane=[true ] Component=[JTextPane  ][                                        ]
	IsTextPane=[false] Component=[ScrollBar  ][null                                    ]
	IsTextPane=[false] Component=[ScrollBar  ][null                                    ]
 * </pre>
 * 
 * 
 * 
 * @author ogattaz
 *
 */
public class GUIComponentSearcher<T> {

	private final Class<T> pGenericType;

	private int pIndex = 0;

	private final Container pRootContainer;

	private final int pTargetIndex;

	/**
	 * @param aRootContainer
	 */
	public GUIComponentSearcher(final Class<T> aGenericType,
			final Container aRootContainer, final int aTargetIndex) {
			// final Container aRootContainer, final int aTargetIndex) {
		super();
		pGenericType = aGenericType;
		pRootContainer = aRootContainer;
		pTargetIndex = aTargetIndex;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public T search() throws Exception {

		// raz
		pIndex = 0;

		T wGUIComponent = searchOne(pRootContainer);

		if (wGUIComponent == null) {

			throw new Exception(String.format(
					"Unable to retrieve the %dth component [%s] Result",
					pTargetIndex, ParameterizedType.class.getSimpleName()));
		}
		return wGUIComponent;
	}

	/**
	 * @param aContainer
	 * @return
	 */
	private T searchOne(Container aContainer) {

		for (Component wComponent : aContainer.getComponents()) {

			if (pGenericType.isInstance(wComponent)) {

				// increment
				pIndex++;

				@SuppressWarnings("unchecked")
				T wGUIComponent = (T) wComponent;

				if (pIndex >= pTargetIndex) {
					return wGUIComponent;
				}
			}
			if (wComponent instanceof Container) {
				T wGUIComponent = searchOne((Container) wComponent);
				if (wGUIComponent != null) {
					return wGUIComponent;
				}
			}
		}
		return null;
	}
}
