/*!\file task_location.h
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Created by: Bruno Oliveira - contact: bruno.scherer@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief
 * This module defines function relative to boarder routing.
 * \detailed
 * 
 */

#include "boarder_route.h"
#include "../../include/kernel_pkg.h"
#include "utils.h"

/**
 * \brief This structure stores the location (slave process address) of the other task
 */

int boarder_route(unsigned int f1, unsigned int f2, unsigned int f3){
	if (f1 == 1){
		if (f2 == 1 and f3 == 1){
			return 57344;
		}
		else if (f2 == 1 and f3 == 0) {
			return 49152;
		}
		else if (f2 == 0 and f3 == 1){
			return 40960;
		}
		else if (f2 == 0 and f3 == 0){
			return 32768;
		}
	}
}

#endif /* BORDER_ROUTE_H_ */
