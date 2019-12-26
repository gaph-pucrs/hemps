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
 * This module defines function relative to border routing.
 * \detailed
 * 
 */

#ifndef BORDER_ROUTE
#define BORDER_ROUTE_H_

#include "../../include/kernel_pkg.h"
#include "border_route.h"
/**
 * \brief This structure stores the location (slave process address) of the other task
 */
struct packed_struct {
  unsigned int f1:1;
  unsigned int f2:1;
  unsigned int f3:1;
}

int border_route(unsigned int f1, unsigned int f2, unsigned int f3);

#endif /* BORDER_ROUTE_H_ */
