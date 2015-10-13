/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.liutils.vis.editor.registry;

import cn.annoreg.base.RegistrationInstance;
import cn.annoreg.core.AnnotationData;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegistryTypeDecl;
import cn.liutils.api.gui.AuxGui;
import cn.liutils.registry.AuxGuiRegistry.RegAuxGui;
import cn.liutils.vis.editor.CommandVis;
import cn.liutils.vis.editor.IVisPluginCommand;

/**
 * @author WeAthFolD
 */
@RegistryTypeDecl
public class VisPluginCommandRegistration extends RegistrationInstance<RegVisPluginCommand, IVisPluginCommand>  {

	public VisPluginCommandRegistration() {
		super(RegVisPluginCommand.class, "VisPluginCommand");
		setLoadStage(LoadStage.INIT);
	}

	@Override
	protected void register(IVisPluginCommand obj, RegVisPluginCommand anno) throws Exception {
		CommandVis.register(anno.value(), obj);
	}
	
}
