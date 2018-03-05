/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/*
 * generated by Xtext
 */
package org.eclipse.smarthome.model.ui.contentassist

import java.io.File
import org.eclipse.core.runtime.CoreException
import org.eclipse.emf.ecore.EObject
import org.eclipse.smarthome.core.items.GroupItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.designer.core.config.ConfigurationFolderProvider
import org.eclipse.smarthome.designer.ui.UIActivator
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content assistant
 */
class SitemapProposalProvider extends AbstractSitemapProposalProvider {

	/* the image location inside the installation folder */
	protected static final String IMAGE_LOCATION = "../webapps/images/";

	override void complete_GroupItemRef(EObject model, RuleCall ruleCall,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.complete_GroupItemRef(model, ruleCall, context, acceptor);

		val registry = UIActivator.itemRegistryTracker.getService();
		if(registry!=null) {
			for(Item item : registry.getItems(context.getPrefix() + "*")) {
				if(item instanceof GroupItem) {
					val completionProposal = createCompletionProposal(item.getName(), context);
					acceptor.accept(completionProposal);
				}
			}
		}
	}

	override void complete_ItemRef(EObject model, RuleCall ruleCall,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.complete_ItemRef(model, ruleCall, context, acceptor);

		val registry = UIActivator.itemRegistryTracker.getService();
		if(registry!=null) {
			for(Item item : registry.getItems(context.getPrefix() + "*")) {
				val completionProposal = createCompletionProposal(item.getName(), context);
				acceptor.accept(completionProposal);
			}
		}
	}

	override void complete_Icon(EObject model, RuleCall ruleCall,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		super.complete_Icon(model, ruleCall, context, acceptor);
		
		try {
			val iconsFolder = ConfigurationFolderProvider.getRootConfigurationFolder().getLocation().toFile().getAbsolutePath() + File.separator + IMAGE_LOCATION;
			val folder = new File(iconsFolder);
			if(folder.isDirectory()) {
				for(String filename : folder.list()) {
					if(filename.toLowerCase().endsWith(".png")) {
						val completionProposal = createCompletionProposal(filename.substring(0, filename.length()-4), context);
						acceptor.accept(completionProposal);
					}
				}
			}
		} catch (CoreException e) {}
	}
}