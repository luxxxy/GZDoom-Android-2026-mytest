#include "Button.h"
#include "TouchControlsConfig.h"

#ifdef __IOS__
#include <sys/time.h>
#endif

#define REPEAT_START_TIME 500
#define REPEAT_INTERVAL   150


using namespace touchcontrols;

Button::Button(std::string tag, RectF pos, std::string image_filename, int value_, bool repeat_, bool hidden_, std::string description_, uint32_t color_) :
        ControlSuper(TC_TYPE_BUTTON, tag, pos)
{
    value = value_;
    image = image_filename;
    description = description_;
    glTex[0] = -1;
    glTex[1] = -1;
    id = -1;
    repeat = repeat_;
    hidden = hidden_;
    color = color_;
    updateSize();

    flash = false;
    flashDir = false;
    flashCount = 0;

    drawAlpha = 1.0f; // multiplier: 1.0 leaves the TouchControls' alpha unchanged
}

void Button::updateSize()
{
    glRect.resize(controlPos.right - controlPos.left, controlPos.bottom - controlPos.top);
}

void Button::setFlash(bool v)
{
    flash = v;
}

void Button::setImage(int32_t i)
{
    if(i < 2)
        glTexDraw = i;
    else
        glTexDraw = 0;
}

void Button::setAlpha(float a)
{
    drawAlpha = a;
}

void Button::resetOutput()
{
    id = -1;
    signal_buttonUp.emit(value);
    signal_button.emit(0, value);
}

bool Button::processPointer(int action, int pid, float x, float y)
{
    if(hidden)  //Hidden controls do not respond to inputs
        return false;

    if(value == -1)
        return false;

    if(action == P_DOWN)
    {
        if(controlPos.contains(x, y))
        {
            id = pid;
            signal_buttonDown.emit(value);
            signal_button.emit(1, value);
            repeatTime = getMS() + REPEAT_START_TIME;//Wait before repeating
            return true;
        }

        return false;
    }
    else if(action == P_UP)
    {
        if(pid == id)
        {
            id = -1;
            signal_buttonUp.emit(value);
            signal_button.emit(0, value);
            return true;
        }

        return false;
    }
    else if(action == P_MOVE)
    {
        return false;
    }

    return false;
}

bool Button::initGL()
{
    std::vector <std::string> images;

    std::istringstream iss(image);

    for(std::string token; std::getline(iss, token, ';');)
    {
        images.push_back(std::move(token));
    }

    int x, y;

    if(images.size() > 0)
    {
        glTex[0] = loadTextureFromPNG(images[0], x, y);
    }

    if(images.size() > 1)
    {
        glTex[1] = loadTextureFromPNG(images[1], x, y);
    }

    return false;
}

bool Button::drawGL(bool forEditor)
{
    if(forEditor)
    {
        if(!hidden)
        {
            gl_drawRect(glTex[0], controlPos.left, controlPos.top, glRect);
        }
    }
    else //Draw normal in game
    {
        if(!hidden)
        {
            if(flash)
            {
                if(getMS() > flashCount)
                {
                    flashCount = getMS() + 300;
                    flashDir = !flashDir;
                }

                if(flashDir)
                    return false;
            }

            // Per-button alpha multiplier (e.g. dim an unavailable action). The
            // owning TouchControls already set the colour/alpha; scale that alpha,
            // preserving the RGB. 1.0 (default) leaves it untouched.
            if(drawAlpha != 1.0f)
            {
                GLfloat r, g, b, a;
                gl_getColor(r, g, b, a);
                gl_color4f(r, g, b, a * drawAlpha);
            }

            // Modern HUD treatment for the primary gameplay actions. Keep the
            // original control textures and hitboxes, but give the important
            // buttons a dark glass panel and a restrained warm outline.
            const bool hudAction = (tag == "back" || tag == "attack" || tag == "attack2" ||
                                    tag == "use" || tag == "show_weapons" ||
                                    tag == "jump" || tag == "next_weapon" ||
                                    tag == "prev_weapon");
            if(hudAction)
            {
                GLfloat originalR, originalG, originalB, originalA;
                gl_getColor(originalR, originalG, originalB, originalA);
                const float x = controlPos.left;
                const float y = controlPos.top;
                const float w = controlPos.right - controlPos.left;
                const float h = controlPos.bottom - controlPos.top;
                const float pad = 0.12f;
                const float edge = 0.045f;
                GLRect panel;
                GLRect topEdge, bottomEdge, leftEdge, rightEdge;
                panel.resize(w + pad * 2.0f, h + pad * 2.0f);
                gl_drawRect(0.035f, 0.045f, 0.055f, 0.72f,
                            x - pad, y - pad, panel);

                // Four fine amber-grey strokes form a consistent HUD frame.
                topEdge.resize(w + pad * 2.0f, edge);
                bottomEdge.resize(w + pad * 2.0f, edge);
                leftEdge.resize(edge, h + pad * 2.0f);
                rightEdge.resize(edge, h + pad * 2.0f);
                gl_drawRect(0.72f, 0.53f, 0.28f, 0.72f,
                            x - pad, y - pad, topEdge);
                gl_drawRect(0.72f, 0.53f, 0.28f, 0.72f,
                            x - pad, y + h + pad - edge, bottomEdge);
                gl_drawRect(0.72f, 0.53f, 0.28f, 0.72f,
                            x - pad, y - pad, leftEdge);
                gl_drawRect(0.72f, 0.53f, 0.28f, 0.72f,
                            x + w + pad - edge, y - pad, rightEdge);
                // Restore the owning HUD's tint/opacity before drawing the icon.
                gl_color4f(originalR, originalG, originalB, originalA);
            }

            gl_drawRect(glTex[glTexDraw], controlPos.left, controlPos.top, glRect);
        }
    }

    if(repeat)
    {
        if(id != -1)
        {
            if(getMS() > repeatTime)
            {
                repeatTime = getMS() + REPEAT_INTERVAL;

                signal_buttonUp.emit(value);
                signal_button.emit(0, value);

                signal_buttonDown.emit(value);
                signal_button.emit(1, value);
            }
        }
    }

    return false;
}

void Button::saveXML(TiXmlDocument &doc)
{
    TiXmlElement *root = new TiXmlElement(tag.c_str());
    doc.LinkEndChild(root);

    ControlSuper::saveXML(*root);
}

void Button::loadXML(TiXmlDocument &doc)
{
    TiXmlHandle hDoc(&doc);
    TiXmlElement *pElem = hDoc.FirstChild(tag).Element();

    if(!pElem)  //Check exists, if not just leave as default
        return;

    ControlSuper::loadXML(*pElem);
}
