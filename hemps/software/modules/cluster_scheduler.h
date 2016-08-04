/*!\file cluster_scheduler.h
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief This module defines the function relative to mapping a new app and a new task into a given slave processor.
 * This module is only used by manager kernel
 */

#ifndef CLUSTER_SCHEDULER_H_
#define CLUSTER_SCHEDULER_H_

inline void allocate_cluster_resource(int, int);

inline void release_cluster_resources(int, int);

void page_used(int, int, int);

void page_released(int, int, int);

int map_task(int);

int application_mapping(int, int);

int SearchCluster(int, int);

/*#if defined(WithoutLoad) || defined(WithProfiler)
int SearchCluster(int app_task_number);
#endif

#ifdef WithLoad
int SearchCluster(int app_task_number);
#endif

#ifdef WithLoad_RealTime
int SearchCluster(int app_task_number);
#endif
*/

#endif /* SOFTWARE_INCLUDE_MAPPING_MAPPING_H_ */
