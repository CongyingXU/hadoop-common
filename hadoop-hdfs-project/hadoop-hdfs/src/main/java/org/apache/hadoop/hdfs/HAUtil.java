/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs;

import static org.apache.hadoop.hdfs.DFSConfigKeys.*;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.apache.hadoop.HadoopIllegalArgumentException;
import org.apache.hadoop.conf.Configuration;

public class HAUtil {
  private HAUtil() { /* Hidden constructor */ }

  /**
   * Returns true if HA for namenode is configured.
   * 
   * @param conf Configuration
   * @return true if HA is configured in the configuration; else false.
   */
  public static boolean isHAEnabled(Configuration conf) {
    Collection<String> collection = DFSUtil.getNameNodeIds(conf);
    return collection != null && !collection.isEmpty();
  }

  /**
   * Get the namenode Id by matching the {@code addressKey}
   * with the the address of the local node.
   * 
   * If {@link DFSConfigKeys#DFS_HA_NAMENODE_ID_KEY} is not specifically
   * configured, this method determines the namenode Id by matching the local
   * node's address with the configured addresses. When a match is found, it
   * returns the namenode Id from the corresponding configuration key.
   * 
   * @param conf Configuration
   * @return namenode Id on success, null on failure.
   * @throws HadoopIllegalArgumentException on error
   */
  public static String getNameNodeId(Configuration conf) {
    String namenodeId = conf.get(DFS_HA_NAMENODE_ID_KEY);
    if (namenodeId != null) {
      return namenodeId;
    }
    if (!isHAEnabled(conf)) {
      return null;
    }
    namenodeId = DFSUtil.getSuffixIDs(conf, DFS_NAMENODE_RPC_ADDRESS_KEY,
        DFSUtil.LOCAL_ADDRESS_MATCHER)[1];
    if (namenodeId == null) {
      String msg = "Configuration " + DFS_NAMENODE_RPC_ADDRESS_KEY + 
          " must be suffixed with" + " namenodeId for HA configuration.";
      throw new HadoopIllegalArgumentException(msg);
    }
    return namenodeId;
  }

  /**
   * Similar to
   * {@link DFSUtil#getNameServiceIdFromAddress(Configuration, 
   * InetSocketAddress, String...)}
   */
  public static String getNameNodeIdFromAddress(final Configuration conf, 
      final InetSocketAddress address, String... keys) {
    // Configuration with a single namenode and no nameserviceId
    if (!isHAEnabled(conf)) {
      return null;
    }
    
    String[] ids = DFSUtil.getSuffixIDs(conf, address, keys);
    if (ids != null && ids.length > 1) {
      return ids[1];
    }
    return null;
  }
}
