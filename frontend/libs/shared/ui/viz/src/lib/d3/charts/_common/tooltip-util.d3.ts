import * as d3 from 'd3';

export function createTooltipElement(container: d3.Selection<any, any, any, any>): d3.Selection<HTMLDivElement, unknown, HTMLElement, any> {
    return container
        .append('div')
        .style('opacity', 0)
        .attr('class', 'viz_tooltip')

        .style('position', 'absolute')
        .style('background-color', 'rgba(78, 78, 78, 0.9)')
        .style('color', '#fff')
        .style('padding', '12px 20px')
        .style('font-size', '14px')
        .style('box-shadow', '.125rem .125rem .5rem rgba(0,0,0,.3)');
}


export function addTooltipToHoverables<Hoverable>(
    hoverable: d3.Selection<any, Hoverable, SVGGElement, any>,
    tooltipElement: d3.Selection<HTMLDivElement, unknown, HTMLElement, any>,
    tooltipContentFn: (d: Hoverable) => string,
    tooltipPositionFn: ((mouseEvent: any, d: Hoverable) => void) | undefined,
): void {

    // mouse start
    const mouseover = function(event: any, d: Hoverable): void {

        // position tooltip even before the mouse is moved
        if (tooltipPositionFn) {
            tooltipPositionFn(event, d);
        }

        // show tooltip
        tooltipElement
            .style('opacity', 1)
            .html(tooltipContentFn(d));
    };

    // mouse move
    const mousemove = function(event: any, d: Hoverable): void {
        //positionTooltip
        if (tooltipPositionFn) {
            tooltipPositionFn(event, d);
        }
    };

    // mouse leave
    const mouseleave = function(_event: any, _d: Hoverable): void {
        // hide tooltip
        tooltipElement
            .html('') // otherwise it is still invisibly in front of stuff and will not allow hovering other bars
            .style('opacity', 0);
    };

    hoverable
        .on('mouseover.tooltip', mouseover)
        .on('mousemove.tooltip', mousemove)
        .on('mouseleave.tooltip', mouseleave)

}

export function addHoverEffect<Hoverable>(
    hoverable: d3.Selection<any, Hoverable, SVGGElement, any>,
    hoverInFn: (transition: d3.Transition<any, unknown, null, undefined>) => void,
    hoverOutFn: (transition: d3.Transition<any, unknown, null, undefined>) => void,
): void {

    // mouse start
    const mouseover = function(_event: any, _d: Hoverable): void {

        // fill the hovered rect
        // eslint-disable-next-line  @typescript-eslint/ban-ts-comment
        // @ts-ignore REASON: d3.select(this) inside a function in .on returns the current DOM element
        hoverInFn(d3.select(this)
            .transition('bar-mouseover').duration(100));
    };


    // mouse leave
    const mouseleave = function(_event: any, _d: Hoverable): void {

        // remove hover fill
        // eslint-disable-next-line  @typescript-eslint/ban-ts-comment
        // @ts-ignore REASON: d3.select(this) inside a function in .on returns the current DOM element
        hoverOutFn(d3.select(this)
            .transition('bar-mouseover').duration(500))
    };

    hoverable
        .on('mouseover.effect', mouseover)
        .on('mouseleave.effect', mouseleave)

}

export function positionTooltipNextToMouse(
    event: any,
    tooltipElement: d3.Selection<HTMLDivElement, unknown, HTMLElement, any>,
    wrapperSizeFn: () => { width: number, height: number },
    margin2: { top: number, bottom: number, left: number, right: number },
    tooltipOffset: { top: number, left: number, bottom: number, right: number },
    positionToSwitchLeftRight: number,
    positionToSwitchTopBottom: number,
): void {

    const w = wrapperSizeFn().width;
    const h = wrapperSizeFn().height;
    const offset = tooltipOffset;
    const margin = margin2;

    // position tooltip close to the cursor but where there is enough space
    const mouseX = d3.pointer(event)[0];
    const mouseY = d3.pointer(event)[1];

    // x position
    if (mouseX + margin.left > (positionToSwitchLeftRight * w)) {
        tooltipElement
            .style('left', '')
            .style('right', `${((w - margin.left - mouseX) + offset.left)}px`)
    } else {
        tooltipElement
            .style('left', `${(mouseX + margin.left + offset.right)}px`)
            .style('right', '')
    }
    // y position
    if (mouseY + margin.top > (positionToSwitchTopBottom * h)) {
        tooltipElement
            .style('top', '')
            .style('bottom', `${((h - margin.top - mouseY) + offset.top)}px`)
    } else {
        tooltipElement
            .style('top', `${(mouseY + margin.top + offset.bottom)}px`)
            .style('bottom', '')
    }
}

export function positionTooltipCentered<D extends { x?: number, y?: number }>(
    _event: any,
    tooltipElement: d3.Selection<HTMLDivElement, unknown, HTMLElement, any>,
    hoveredElement: D,
    tooltipOffset: { top: number, left: number },
): void {
    const x = (hoveredElement.x ?? 0) + tooltipOffset.left;
    const y = (hoveredElement.y ?? 0) + tooltipOffset.top;

    // x position
    tooltipElement.style('left', `${x}px`);
    // y position
    tooltipElement.style('top', `${y}px`);
}

export function positionTooltipFixedBottom(
    _event: any,
    tooltipElement: d3.Selection<HTMLDivElement, unknown, HTMLElement, any>,
): void {

    // x position
    tooltipElement.style('left', `0px`);
    // y position
    tooltipElement.style('bottom', `0px`);
}

