/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netease.backend.tcc;

import java.util.List;

import com.netease.backend.tcc.common.HeuristicsInfo;
import com.netease.backend.tcc.error.CoordinatorException;

public interface Coordinator {	
	long begin(int sequenceId, List<Procedure> procedures) throws CoordinatorException;

	short confirm(int sequenceId, long uuid, List<Procedure> procedures) throws CoordinatorException;
	
	short confirm(int sequenceId, long uuid, long timeout, List<Procedure> procedures) throws CoordinatorException;
	
	short cancel(int sequenceId, long uuid, List<Procedure> procedures) throws CoordinatorException;
	
	short cancel(int sequenceId, long uuid, long timeout, List<Procedure> procedures) throws CoordinatorException;
	
	List<HeuristicsInfo> getHeuristicExceptionList(long startTime, long endTime) throws CoordinatorException;
	
	void removeHeuristicExceptions(List<Long> txIdList) throws CoordinatorException;
	
	/*
	 * for test
	 */
	int getTxTableSize();
}