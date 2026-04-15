import { withDevtools } from '@angular-architects/ngrx-toolkit';
import { computed, DestroyRef, inject, Injectable, Signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {KindSaveJson} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import { requireValue } from '@dvb/util-shared';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { Observable, Subject } from 'rxjs';

export interface EventMap {
  UNTERSUCHUNG_ABSCHLIESSEN_REQUESTED: { einladungId: string };
  KIND_DUPLIKAT_VORHANDEN: { kindTriedToSave: KindSaveJson, kindIdsOfDuplicates: BackendEntityId[] };
  IMPFSTOFF_CHARGEN_POTENTIALLY_CHANGED: { impfstoffId: BackendEntityId };
}

export interface EventbusEvent<K extends keyof EventMap = keyof EventMap> {
  type: K;
  payload: EventMap[K];
  timestamp: number;
  id: string;
}

export interface EventbusState {
  events: Array<EventbusEvent>;
  eventStreams: Map<keyof EventMap, Subject<any>>;
  maxEvents: number;
}

const initialState: EventbusState = {
  events: [],
  eventStreams: new Map<keyof EventMap, Subject<any>>(),
  maxEvents: 100, // Configurable event history limit
};

@Injectable({ providedIn: 'root' })
export class EventBusStore extends signalStore(
  { protectedState: false },
  withDevtools('eventbus'),
  withState(initialState),
  withMethods(store => {
    const destroyRef = inject(DestroyRef);

    // Clean up subjects when store is destroyed
    destroyRef.onDestroy(() => {
      store.eventStreams().forEach(subject => subject.complete());
    });

    return {
      emit<K extends keyof EventMap>(type: K, payload: EventMap[K]): void {
        const event: EventbusEvent<K> = {
          type,
          payload,
          timestamp: Date.now(),
          id: crypto.randomUUID(),
        };

        // Update signal state with event history limit
        patchState(store, state => {
          const newEvents = [...state.events, event];
          return {
            events: newEvents.length > state.maxEvents ? newEvents.slice(-state.maxEvents) : newEvents,
          };
        });

        // Emit to observable stream
        this.getOrCreateStream(type).next(payload);
      },

      // Type-safe observable stream with cleanup
      on<K extends keyof EventMap>(type: K): Observable<EventMap[K]> {
        return this.getOrCreateStream(type).asObservable().pipe(takeUntilDestroyed(destroyRef));
      },

      // Get or create stream with proper typing
      getOrCreateStream<K extends keyof EventMap>(type: K): Subject<EventMap[K]> {
        const streams = store.eventStreams();
        if (!streams.has(type)) {
          const newSubject = new Subject<EventMap[K]>();
          streams.set(type, newSubject);
          patchState(store, { eventStreams: new Map(streams) });
        }
        return requireValue(streams.get(type)) as Subject<EventMap[K]>;
      },

      // Type-safe signal access with memoization
      lastEventOfType<K extends keyof EventMap>(type: K): Signal<EventMap[K] | null> {
        return computed(() => {
          const events = store.events();
          for (let i = events.length - 1; i >= 0; i--) {
            if (events[i].type === type) {
              return events[i].payload as EventMap[K];
            }
          }
          return null;
        });
      },

      // All events of specific type with better performance
      eventsOfType<K extends keyof EventMap>(type: K): Signal<EventMap[K][]> {
        return computed(() =>
          store
            .events()
            .filter((event): event is EventbusEvent<K> => event.type === type)
            .map(event => event.payload),
        );
      },

      // New utility methods
      clearEvents(): void {
        patchState(store, { events: [] });
      },

      clearEventsOfType<K extends keyof EventMap>(type: K): void {
        patchState(store, state => ({
          events: state.events.filter(event => event.type !== type),
        }));
      },

      getEventCount(): Signal<number> {
        return computed(() => store.events().length);
      },

      getEventCountByType<K extends keyof EventMap>(type: K): Signal<number> {
        return computed(() => store.events().filter(event => event.type === type).length);
      },

      setMaxEvents(maxEvents: number): void {
        patchState(store, state => {
          const trimmedEvents = state.events.length > maxEvents ? state.events.slice(-maxEvents) : state.events;

          return {
            maxEvents,
            events: trimmedEvents,
          };
        });
      },
    };
  }),
) {
  // empty body
}
