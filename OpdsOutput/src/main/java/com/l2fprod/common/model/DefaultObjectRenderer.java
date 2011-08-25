/*
 * ====================================================================
 * 
 * L2FProd.com Common Components 7.3 License.
 * 
 * Copyright (c) 2005-2007 L2FProd.com. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowlegement: "This product includes software developed by L2FProd.com
 * (http://www.L2FProd.com/)." Alternately, this acknowlegement may appear in
 * the software itself, if and wherever such third-party acknowlegements
 * normally appear. 4. The names "L2FProd.com Common Components", "l2fprod-common"
 * and "L2FProd.com" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written permission,
 * please contact info@L2FProd.com. 5. Products derived from this software may
 * not be called "l2fprod-common" nor may "l2fprod-common" appear in
 * their names without prior written permission of L2FProd.com.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * L2FPROD.COM OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.l2fprod.common.model;

import com.l2fprod.common.util.ResourceManager;
import com.l2fprod.common.util.converter.ConverterRegistry;

import java.io.File;

/**
 * DefaultObjectRenderer. <br>
 *  
 */
public class DefaultObjectRenderer implements ObjectRenderer {

  private boolean idVisible = false;

  public void setIdVisible(boolean b) {
    idVisible = b;
  }

  public String getText(Object object) {
    if (object == null) {
      return null;
    }

    // lookup the shared ConverterRegistry
    try {
      return (String)ConverterRegistry.instance().convert(String.class, object);
    } catch (IllegalArgumentException e) {
    }

    if (object instanceof Boolean) {
      return Boolean.TRUE.equals(object)
        ? ResourceManager.common().getString("true")
        : ResourceManager.common().getString("false");
    }

    if (object instanceof File) {
      return ((File)object).getAbsolutePath();
    }

    StringBuffer buffer = new StringBuffer();
    if (idVisible && object instanceof HasId) {
      buffer.append(((HasId)object).getId());
    }
    if (object instanceof TitledObject) {
      buffer.append(((TitledObject)object).getTitle());
    }
    if (!(object instanceof HasId || object instanceof TitledObject)) {
      buffer.append(String.valueOf(object));
    }
    return buffer.toString();
  }

}
