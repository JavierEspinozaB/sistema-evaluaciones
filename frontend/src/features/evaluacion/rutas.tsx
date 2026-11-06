import type { RouteObject } from 'react-router';
import { SubpruebaRunner } from './components/SubpruebaRunner';

export const rutasEvaluacion: RouteObject[] = [
  {
    path: 'evaluaciones/:evaluacionId/subpruebas/:codigo',
    element: <SubpruebaRunner />,
  },
];
