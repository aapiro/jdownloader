//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.swing.jdgui.settings.panels.premium.Columns;

import java.awt.Component;

import javax.swing.JTable;

import jd.gui.swing.components.table.JDTableColumn;
import jd.gui.swing.components.table.JDTableModel;
import jd.gui.swing.jdgui.components.JDProgressBarRender;
import jd.gui.swing.jdgui.settings.panels.premium.HostAccounts;
import jd.nutils.Formatter;
import jd.plugins.Account;
import jd.plugins.AccountInfo;

import org.jdesktop.swingx.renderer.JRendererLabel;

public class TrafficLeftColumn extends JDTableColumn {

    private JDProgressBarRender progress;
    private JRendererLabel jlr;

    public TrafficLeftColumn(String name, JDTableModel table) {
        super(name, table);
        progress = new JDProgressBarRender();
        progress.setStringPainted(true);
        progress.setOpaque(true);
        jlr = new JRendererLabel();
        jlr.setBorder(null);
    }

    private static final long serialVersionUID = -5291590062503352550L;
    private Component co;

    @Override
    public Component myTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Component myTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Account) {
            Account ac = (Account) value;
            AccountInfo ai = ac.getAccountInfo();
            if (!ac.isValid()) {
                value = "Invalid account";
                progress.setMaximum(10);
                progress.setValue(0);
            } else if (ai == null) {
                value = "Unknown";
                progress.setMaximum(10);
                progress.setValue(0);
            } else {
                if (ai.isUnlimitedTraffic()) {
                    value = "Unlimited";
                    progress.setMaximum(10);
                    progress.setValue(10);
                } else {
                    value = Formatter.formatReadable(ai.getTrafficLeft()) + "/" + Formatter.formatReadable(ai.getTrafficMax());
                    progress.setMaximum(ai.getTrafficMax());
                    progress.setValue(ai.getTrafficLeft());
                }
            }
            progress.setString((String) value);
            co = progress;
        } else {
            HostAccounts ha = (HostAccounts) value;
            if (!ha.gotAccountInfos()) {
                jlr.setText("Unknown");
            } else {
                if (ha.getTraffic() < 0) {
                    jlr.setText("Unlimited");
                } else if (ha.getTraffic() == 0) {
                    jlr.setText("No Traffic left");
                } else {
                    jlr.setText(Formatter.formatReadable(ha.getTraffic()));
                }
            }
            co = jlr;
        }
        return co;
    }

    @Override
    public void postprocessCell(Component c, JTable table, Object value, boolean isSelected, int row, int column) {
        if (!(value instanceof Account)) {
            c.setBackground(table.getBackground().darker());
        }
    }

    @Override
    public boolean isEditable(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setValue(Object value, Object object) {
        // TODO Auto-generated method stub

    }

    public Object getCellEditorValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSortable(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sort(Object obj, boolean sortingToggle) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isEnabled(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Account) return ((Account) obj).isEnabled();
        if (obj instanceof HostAccounts) return ((HostAccounts) obj).isEnabled();
        return true;
    }

}
