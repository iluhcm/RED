/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.datatypes.sites;

/**
 * The listener interface for receiving SiteListChange events. The class that is interested in processing a SiteListChange event implements this interface,
 * and the object created with that class is registered with a component using the component's <code>addSiteListChangeListener<code> method. When
 * the SiteListChange event occurs, that object's appropriate method is invoked.
 */
public interface SiteListChangeListener {

    /**
     * Site list added.
     *
     * @param l the l
     */
    public void siteListAdded(SiteList l);

    /**
     * Site list removed.
     *
     * @param l the l
     */
    public void siteListRemoved(SiteList l);

    /**
     * Site list renamed.
     *
     * @param l the l
     */
    public void siteListRenamed(SiteList l);

}
