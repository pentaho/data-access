/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License, version 2 as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html or from the Free Software Foundation, Inc.,  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2018 Hitachi Vantara.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.datasource.api;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Dmitriy Stepanov on 23.05.18.
 */
public class DatasourceLock implements IDatasourceLock {

  protected static ReadWriteLock lock = new ReentrantReadWriteLock(  );

  @Override public void lockRead() {
    lock.readLock().lock();
  }

  @Override public void unlockRead() {
    lock.readLock().unlock();
  }

  @Override public void lockWrite() {
    lock.writeLock().lock();
  }

  @Override public void unlockWrite() {
    lock.writeLock().unlock();
  }
}
